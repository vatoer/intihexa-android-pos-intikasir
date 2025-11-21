/**
 * Production Server dengan Encrypted Request/Response
 *
 * New Flow:
 * 1. Client encrypts { sn, device_uuid } with public key → cipher
 * 2. Client sends { cipher }
 * 3. Server decrypts cipher with private key
 * 4. Server validates and creates response payload
 * 5. Server signs payload with private key → signature
 * 6. Server sends { ok, payload (base64), signature }
 * 7. Client decodes payload and verifies signature
 */

require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const fs = require('fs');
const { Pool } = require('pg');
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const cors = require('cors');

const app = express();

// Security middleware
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
  methods: ['POST', 'GET'],
  credentials: true
}));
app.use(bodyParser.json());

// Rate limiting
const activationLimiter = rateLimit({
  windowMs: 60 * 60 * 1000,
  max: 5,
  message: {
    ok: false,
    error: 'RATE_LIMIT',
    message: 'Terlalu banyak percobaan aktivasi. Silakan coba lagi nanti.',
    payload: null,
    signature: null
  }
});

// Database
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 5432,
  database: process.env.DB_NAME || 'intikasir',
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD,
  max: 20
});

// Load RSA keys
let PRIVATE_KEY, PUBLIC_KEY;
try {
  PRIVATE_KEY = fs.readFileSync('./keys/private_key.pem', 'utf8');
  PUBLIC_KEY = fs.readFileSync('./keys/public_key.pem', 'utf8');
  console.log('✓ RSA keys loaded successfully');
} catch (error) {
  console.error('✗ Failed to load RSA keys:', error.message);
  process.exit(1);
}

// RSA Decrypt with OAEP
function rsaPrivateDecrypt(cipherBase64) {
  try {
    const buffer = Buffer.from(cipherBase64, 'base64');
    const decrypted = crypto.privateDecrypt(
      {
        key: PRIVATE_KEY,
        padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
        oaepHash: 'sha256',
      },
      buffer
    );
    return decrypted.toString('utf8');
  } catch (error) {
    throw new Error(`Decryption failed: ${error.message}`);
  }
}

// RSA Sign with SHA-256
function rsaSign(data) {
  const sign = crypto.createSign('SHA256');
  sign.update(data);
  sign.end();
  return sign.sign(PRIVATE_KEY, 'base64');
}

// Audit log
async function auditLog(action, serialNumber, deviceUuid, success, message, ipAddress) {
  try {
    await pool.query(
      `INSERT INTO activation_audit_logs
       (action, serial_number, device_id, success, message, ip_address)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [action, serialNumber, deviceUuid, success, message, ipAddress]
    );
  } catch (error) {
    console.error('Audit log error:', error);
  }
}

// Main activation endpoint
app.post('/api/activate', activationLimiter, async (req, res) => {
  const { cipher } = req.body;
  const ipAddress = req.ip || req.connection.remoteAddress;

  console.log(`[${new Date().toISOString()}] Activation request from ${ipAddress}`);

  try {
    // 1. Validate request
    if (!cipher) {
      return res.json({
        ok: false,
        error: 'INVALID_REQUEST',
        message: 'Cipher harus diisi',
        payload: null,
        signature: null
      });
    }

    // 2. Decrypt cipher
    let requestData;
    try {
      const decrypted = rsaPrivateDecrypt(cipher);
      requestData = JSON.parse(decrypted);
    } catch (error) {
      console.error('Decryption error:', error.message);
      await auditLog('ACTIVATE', null, null, false, 'Decryption failed', ipAddress);
      return res.json({
        ok: false,
        error: 'DECRYPTION_FAILED',
        message: 'Gagal mendekripsi request. Periksa konfigurasi.',
        payload: null,
        signature: null
      });
    }

    const { sn: serialNumber, device_uuid: deviceUuid } = requestData;

    console.log(`  SN: ${serialNumber}, Device: ${deviceUuid}`);

    // 3. Validate data
    if (!serialNumber || !deviceUuid) {
      await auditLog('ACTIVATE', serialNumber, deviceUuid, false, 'Missing parameters', ipAddress);
      return res.json({
        ok: false,
        error: 'INVALID_DATA',
        message: 'Serial Number dan Device UUID harus diisi',
        payload: null,
        signature: null
      });
    }

    // 4. Check Serial Number
    const snResult = await pool.query(
      'SELECT * FROM serial_numbers WHERE serial_number = $1',
      [serialNumber]
    );

    if (snResult.rows.length === 0) {
      await auditLog('ACTIVATE', serialNumber, deviceUuid, false, 'Invalid serial number', ipAddress);
      return res.json({
        ok: false,
        error: 'INVALID_SERIAL',
        message: 'Serial Number tidak valid. Silakan hubungi admin.',
        payload: null,
        signature: null
      });
    }

    const snInfo = snResult.rows[0];

    // 5. Check if already used
    if (snInfo.is_used) {
      await auditLog('ACTIVATE', serialNumber, deviceUuid, false, 'Serial already used', ipAddress);
      return res.json({
        ok: false,
        error: 'SERIAL_USED',
        message: 'Serial Number sudah digunakan',
        payload: null,
        signature: null
      });
    }

    // 6. Check if expired
    if (snInfo.expiry_date && new Date(snInfo.expiry_date) < new Date()) {
      await auditLog('ACTIVATE', serialNumber, deviceUuid, false, 'Serial expired', ipAddress);
      return res.json({
        ok: false,
        error: 'SERIAL_EXPIRED',
        message: 'Serial Number sudah kadaluarsa',
        payload: null,
        signature: null
      });
    }

    // 7. Check device activation
    const deviceResult = await pool.query(
      'SELECT * FROM activations WHERE device_id = $1 AND is_active = true',
      [deviceUuid]
    );

    if (deviceResult.rows.length > 0) {
      const existing = deviceResult.rows[0];
      await auditLog('ACTIVATE', serialNumber, deviceUuid, false, 'Device already activated', ipAddress);
      return res.json({
        ok: false,
        error: 'DEVICE_ACTIVATED',
        message: `Device sudah diaktivasi dengan SN: ${existing.serial_number}`,
        payload: null,
        signature: null
      });
    }

    // 8. Calculate expiry
    let expiryMonths = 12;
    switch (snInfo.tier) {
      case 'trial': expiryMonths = 1; break;
      case 'basic': expiryMonths = 12; break;
      case 'pro': expiryMonths = 12; break;
      case 'enterprise': expiryMonths = 24; break;
    }

    const expiry = Date.now() + (expiryMonths * 30 * 24 * 60 * 60 * 1000);

    // 9. Create response payload
    const responsePayload = {
      sn: serialNumber,
      device_uuid: deviceUuid,
      expiry: expiry,
      tier: snInfo.tier
    };

    const payloadJson = JSON.stringify(responsePayload);
    const payloadBase64 = Buffer.from(payloadJson, 'utf8').toString('base64');

    // 10. Sign payload
    const signature = rsaSign(payloadJson);

    // 11. Save to database
    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      await client.query(
        `INSERT INTO activations
         (serial_number, device_id, expiry_timestamp, tier, ip_address)
         VALUES ($1, $2, $3, $4, $5)`,
        [serialNumber, deviceUuid, expiry, snInfo.tier, ipAddress]
      );

      await client.query(
        `UPDATE serial_numbers
         SET is_used = true, used_at = NOW(), used_by_device = $1
         WHERE serial_number = $2`,
        [deviceUuid, serialNumber]
      );

      await client.query('COMMIT');

      await auditLog('ACTIVATE', serialNumber, deviceUuid, true, 'Activation successful', ipAddress);

      console.log(`✓ Activation successful - SN: ${serialNumber}, Device: ${deviceUuid}`);

      // 12. Return encrypted response
      return res.json({
        ok: true,
        payload: payloadBase64,
        signature: signature,
        message: 'Aktivasi berhasil! Aplikasi siap digunakan.'
      });

    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }

  } catch (error) {
    console.error('Activation error:', error);
    await auditLog('ACTIVATE', null, null, false, `Error: ${error.message}`, ipAddress);

    return res.status(500).json({
      ok: false,
      error: 'SERVER_ERROR',
      message: 'Terjadi kesalahan server. Silakan coba lagi.',
      payload: null,
      signature: null
    });
  }
});

// Health check
app.get('/api/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({
      status: 'ok',
      timestamp: Date.now(),
      database: 'connected'
    });
  } catch (error) {
    res.status(503).json({
      status: 'error',
      timestamp: Date.now(),
      database: 'disconnected'
    });
  }
});

// Admin endpoints (same as before)
app.get('/admin/activations', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        a.id, a.serial_number, a.device_id, a.tier,
        a.activated_at, a.expiry_timestamp, a.is_active,
        EXTRACT(DAY FROM (to_timestamp(a.expiry_timestamp / 1000) - NOW())) as days_remaining
      FROM activations a
      ORDER BY a.activated_at DESC
      LIMIT 100
    `);

    res.json({
      total: result.rows.length,
      activations: result.rows
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/admin/generate-sn', async (req, res) => {
  const { tier = 'basic', count = 1, expiryDays = null } = req.body;

  try {
    const generated = [];
    const expiryDate = expiryDays ? new Date(Date.now() + expiryDays * 24 * 60 * 60 * 1000) : null;

    for (let i = 0; i < count; i++) {
      const sn = `SN-${tier.toUpperCase()}-${Date.now()}-${crypto.randomBytes(4).toString('hex').toUpperCase()}`;

      await pool.query(
        `INSERT INTO serial_numbers (serial_number, tier, expiry_date)
         VALUES ($1, $2, $3)`,
        [sn, tier, expiryDate]
      );

      generated.push({ serialNumber: sn, tier: tier, expiryDate: expiryDate });
    }

    console.log(`✓ Generated ${count} Serial Numbers (${tier})`);

    res.json({
      success: true,
      count: generated.length,
      generated: generated
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Start server
const PORT = process.env.PORT || 3000;
const server = app.listen(PORT, () => {
  console.log('='.repeat(70));
  console.log('  IntiKasir Activation Server (Encrypted)');
  console.log('='.repeat(70));
  console.log(`  Server: http://localhost:${PORT}`);
  console.log(`  Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log('='.repeat(70));
  console.log('  Endpoints:');
  console.log(`    POST   /api/activate (encrypted)`);
  console.log(`    GET    /api/health`);
  console.log(`    GET    /admin/activations`);
  console.log(`    POST   /admin/generate-sn`);
  console.log('='.repeat(70));
});

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('\nShutting down gracefully...');
  server.close(async () => {
    await pool.end();
    console.log('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', async () => {
  console.log('\nShutting down gracefully...');
  server.close(async () => {
    await pool.end();
    console.log('Server closed');
    process.exit(0);
  });
});

