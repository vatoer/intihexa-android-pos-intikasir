/**
 * Production-ready Activation Server untuk IntiKasir
 *
 * Features:
 * - PostgreSQL database
 * - Rate limiting
 * - Security headers
 * - Audit logging
 * - Error handling
 *
 * Setup:
 * 1. npm install
 * 2. Copy .env.example to .env and configure
 * 3. Create database and run schema.sql
 * 4. Put private_key.pem in keys/ folder
 * 5. npm start
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

// Rate limiting for activation endpoint
const activationLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 5, // 5 requests per hour per IP
  message: {
    success: false,
    message: 'Terlalu banyak percobaan aktivasi. Silakan coba lagi nanti.',
    signature: null,
    expiry: null
  },
  standardHeaders: true,
  legacyHeaders: false,
});

// Database connection
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 5432,
  database: process.env.DB_NAME || 'intikasir',
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

// Load private key
let PRIVATE_KEY;
try {
  PRIVATE_KEY = fs.readFileSync('./keys/private_key.pem', 'utf8');
  console.log('✓ Private key loaded successfully');
} catch (error) {
  console.error('✗ Failed to load private key:', error.message);
  console.error('  Make sure private_key.pem exists in ./keys/ folder');
  process.exit(1);
}

// Utility: Generate signature
function generateSignature(serialNumber, deviceId, expiry) {
  const data = `${serialNumber}:${deviceId}:${expiry}`;
  const sign = crypto.createSign('SHA256');
  sign.update(data);
  sign.end();
  const signature = sign.sign(PRIVATE_KEY);
  return signature.toString('base64');
}

// Utility: Audit log
async function auditLog(action, serialNumber, deviceId, success, message, ipAddress) {
  try {
    await pool.query(
      `INSERT INTO activation_audit_logs
       (action, serial_number, device_id, success, message, ip_address)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [action, serialNumber, deviceId, success, message, ipAddress]
    );
  } catch (error) {
    console.error('Audit log error:', error);
  }
}

// Main activation endpoint
app.post('/api/activate', activationLimiter, async (req, res) => {
  const { serialNumber, deviceId } = req.body;
  const ipAddress = req.ip || req.connection.remoteAddress;

  console.log(`[${new Date().toISOString()}] Activation request - SN: ${serialNumber}, Device: ${deviceId}, IP: ${ipAddress}`);

  try {
    // Validate input
    if (!serialNumber || !deviceId) {
      await auditLog('ACTIVATE', serialNumber, deviceId, false, 'Missing parameters', ipAddress);
      return res.status(400).json({
        success: false,
        message: 'Serial Number dan Device ID harus diisi',
        signature: null,
        expiry: null
      });
    }

    // Check if Serial Number exists and valid
    const snResult = await pool.query(
      'SELECT * FROM serial_numbers WHERE serial_number = $1',
      [serialNumber]
    );

    if (snResult.rows.length === 0) {
      await auditLog('ACTIVATE', serialNumber, deviceId, false, 'Invalid serial number', ipAddress);
      return res.status(404).json({
        success: false,
        message: 'Serial Number tidak valid. Silakan hubungi admin.',
        signature: null,
        expiry: null
      });
    }

    const snInfo = snResult.rows[0];

    // Check if already used
    if (snInfo.is_used) {
      await auditLog('ACTIVATE', serialNumber, deviceId, false, 'Serial number already used', ipAddress);
      return res.status(409).json({
        success: false,
        message: 'Serial Number sudah digunakan',
        signature: null,
        expiry: null
      });
    }

    // Check if not expired (if has expiry_date)
    if (snInfo.expiry_date && new Date(snInfo.expiry_date) < new Date()) {
      await auditLog('ACTIVATE', serialNumber, deviceId, false, 'Serial number expired', ipAddress);
      return res.status(410).json({
        success: false,
        message: 'Serial Number sudah kadaluarsa',
        signature: null,
        expiry: null
      });
    }

    // Check if device already activated with different SN
    const deviceResult = await pool.query(
      'SELECT * FROM activations WHERE device_id = $1 AND is_active = true',
      [deviceId]
    );

    if (deviceResult.rows.length > 0) {
      const existingActivation = deviceResult.rows[0];
      await auditLog('ACTIVATE', serialNumber, deviceId, false, 'Device already activated', ipAddress);
      return res.status(409).json({
        success: false,
        message: `Device sudah diaktivasi dengan SN: ${existingActivation.serial_number}`,
        signature: null,
        expiry: null
      });
    }

    // Calculate expiry based on tier
    let expiryMonths = 12; // default 1 year
    switch (snInfo.tier) {
      case 'trial':
        expiryMonths = 1;
        break;
      case 'basic':
        expiryMonths = 12;
        break;
      case 'pro':
        expiryMonths = 12;
        break;
      case 'enterprise':
        expiryMonths = 24;
        break;
    }

    const expiry = Date.now() + (expiryMonths * 30 * 24 * 60 * 60 * 1000);

    // Generate signature
    const signature = generateSignature(serialNumber, deviceId, expiry);

    // Start transaction
    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      // Create activation
      await client.query(
        `INSERT INTO activations
         (serial_number, device_id, expiry_timestamp, tier, ip_address)
         VALUES ($1, $2, $3, $4, $5)`,
        [serialNumber, deviceId, expiry, snInfo.tier, ipAddress]
      );

      // Mark serial number as used
      await client.query(
        `UPDATE serial_numbers
         SET is_used = true, used_at = NOW(), used_by_device = $1
         WHERE serial_number = $2`,
        [deviceId, serialNumber]
      );

      await client.query('COMMIT');

      await auditLog('ACTIVATE', serialNumber, deviceId, true, 'Activation successful', ipAddress);

      console.log(`✓ Activation successful - SN: ${serialNumber}, Device: ${deviceId}`);

      return res.json({
        success: true,
        message: 'Aktivasi berhasil! Aplikasi siap digunakan.',
        signature: signature,
        expiry: expiry
      });

    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }

  } catch (error) {
    console.error('Activation error:', error);
    await auditLog('ACTIVATE', serialNumber, deviceId, false, `Error: ${error.message}`, ipAddress);

    return res.status(500).json({
      success: false,
      message: 'Terjadi kesalahan server. Silakan coba lagi.',
      signature: null,
      expiry: null
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
      database: 'disconnected',
      error: error.message
    });
  }
});

// Admin: List activations
app.get('/admin/activations', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        a.id,
        a.serial_number,
        a.device_id,
        a.tier,
        a.activated_at,
        a.expiry_timestamp,
        a.is_active,
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

// Admin: Generate Serial Numbers
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

      generated.push({
        serialNumber: sn,
        tier: tier,
        expiryDate: expiryDate
      });
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

// Admin: Deactivate device
app.post('/admin/deactivate', async (req, res) => {
  const { deviceId, reason } = req.body;

  try {
    await pool.query(
      `UPDATE activations
       SET is_active = false, deactivated_at = NOW(), deactivation_reason = $1
       WHERE device_id = $2`,
      [reason, deviceId]
    );

    await auditLog('DEACTIVATE', null, deviceId, true, reason, req.ip);

    res.json({
      success: true,
      message: 'Device berhasil dinonaktifkan'
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
  console.log('  IntiKasir Activation Server');
  console.log('='.repeat(70));
  console.log(`  Server: http://localhost:${PORT}`);
  console.log(`  Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log('='.repeat(70));
  console.log('  Endpoints:');
  console.log(`    POST   /api/activate`);
  console.log(`    GET    /api/health`);
  console.log(`    GET    /admin/activations`);
  console.log(`    POST   /admin/generate-sn`);
  console.log(`    POST   /admin/deactivate`);
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

