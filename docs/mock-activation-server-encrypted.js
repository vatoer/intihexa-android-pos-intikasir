/**
 * Mock Server for IntiKasir Activation System (Encrypted Format)
 *
 * New encrypted flow:
 * - Request: { cipher: "base64_encrypted_json" }
 * - Response: { ok: true, payload: "base64_json", signature: "base64_signature" }
 *
 * Install: npm install express body-parser
 * Run: node mock-activation-server-encrypted.js
 */

const express = require('express');
const bodyParser = require('body-parser');
const crypto = require('crypto');

const app = express();
app.use(bodyParser.json());

// In-memory database
const activations = new Map();
const serialNumbers = new Map([
  ['SN-DEMO-00001', { used: false, tier: 'basic' }],
  ['SN-DEMO-00002', { used: false, tier: 'basic' }],
  ['SN-DEMO-00003', { used: false, tier: 'pro' }],
]);

// Mock RSA keys (use real keys in production!)
const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' }
});

console.log('🔑 Mock RSA Keys Generated');
console.log('📋 Public Key (use this in Android - RsaEncryption.kt):');
console.log(publicKey.split('\n').filter(l => !l.includes('BEGIN') && !l.includes('END')).join(''));
console.log();

// Decrypt with private key
function rsaPrivateDecrypt(cipherBase64) {
  try {
    const buffer = Buffer.from(cipherBase64, 'base64');
    const decrypted = crypto.privateDecrypt(
      {
        key: privateKey,
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

// Sign with private key
function rsaSign(data) {
  const sign = crypto.createSign('SHA256');
  sign.update(data);
  sign.end();
  return sign.sign(privateKey, 'base64');
}

// Main endpoint
app.post('/api/activate', (req, res) => {
  const { cipher } = req.body;

  console.log(`[ACTIVATION REQUEST]`);
  console.log(`  Cipher length: ${cipher?.length || 0} bytes`);

  try {
    // Validate
    if (!cipher) {
      return res.json({
        ok: false,
        error: 'INVALID_REQUEST',
        message: 'Cipher harus diisi',
        payload: null,
        signature: null
      });
    }

    // Decrypt
    let requestData;
    try {
      const decrypted = rsaPrivateDecrypt(cipher);
      requestData = JSON.parse(decrypted);
      console.log(`  Decrypted: ${decrypted}`);
    } catch (error) {
      console.log(`  ❌ Decryption failed: ${error.message}`);
      return res.json({
        ok: false,
        error: 'DECRYPTION_FAILED',
        message: 'Gagal mendekripsi request. Pastikan menggunakan public key yang benar.',
        payload: null,
        signature: null
      });
    }

    const { sn, device_uuid } = requestData;
    console.log(`  SN: ${sn}`);
    console.log(`  Device: ${device_uuid}`);

    // Validate
    if (!sn || !device_uuid) {
      return res.json({
        ok: false,
        error: 'INVALID_DATA',
        message: 'Serial Number dan Device UUID harus diisi',
        payload: null,
        signature: null
      });
    }

    // Check SN
    const snInfo = serialNumbers.get(sn);
    if (!snInfo) {
      console.log(`  ❌ Serial Number tidak ditemukan`);
      return res.json({
        ok: false,
        error: 'INVALID_SERIAL',
        message: 'Serial Number tidak valid. Silakan hubungi admin.',
        payload: null,
        signature: null
      });
    }

    // Check if used
    if (snInfo.used) {
      console.log(`  ❌ Serial Number sudah digunakan`);
      return res.json({
        ok: false,
        error: 'SERIAL_USED',
        message: 'Serial Number sudah digunakan di perangkat lain',
        payload: null,
        signature: null
      });
    }

    // Check device
    const existingActivation = Array.from(activations.values()).find(
      act => act.device_uuid === device_uuid
    );
    if (existingActivation) {
      console.log(`  ❌ Device sudah diaktivasi`);
      return res.json({
        ok: false,
        error: 'DEVICE_ACTIVATED',
        message: `Device sudah diaktivasi dengan SN: ${existingActivation.sn}`,
        payload: null,
        signature: null
      });
    }

    // Calculate expiry (1 year)
    const expiry = Date.now() + (365 * 24 * 60 * 60 * 1000);

    // Create response payload
    const responsePayload = {
      sn: sn,
      device_uuid: device_uuid,
      expiry: expiry,
      tier: snInfo.tier
    };

    const payloadJson = JSON.stringify(responsePayload);
    const payloadBase64 = Buffer.from(payloadJson, 'utf8').toString('base64');

    // Sign payload
    const signature = rsaSign(payloadJson);

    // Save
    activations.set(sn, {
      sn: sn,
      device_uuid: device_uuid,
      expiry: expiry,
      tier: snInfo.tier,
      activatedAt: Date.now()
    });
    snInfo.used = true;

    console.log(`  ✅ Aktivasi berhasil`);
    console.log(`  Total aktivasi: ${activations.size}`);

    // Return
    return res.json({
      ok: true,
      payload: payloadBase64,
      signature: signature,
      message: 'Aktivasi berhasil! Aplikasi siap digunakan.'
    });

  } catch (error) {
    console.error(`  ❌ Error:`, error);
    return res.json({
      ok: false,
      error: 'SERVER_ERROR',
      message: 'Terjadi kesalahan server',
      payload: null,
      signature: null
    });
  }
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: Date.now(),
    totalActivations: activations.size,
    availableSerialNumbers: Array.from(serialNumbers.values()).filter(sn => !sn.used).length
  });
});

// Admin
app.get('/admin/activations', (req, res) => {
  const activationList = Array.from(activations.values()).map(act => ({
    sn: act.sn,
    device_uuid: act.device_uuid,
    tier: act.tier,
    activatedAt: new Date(act.activatedAt).toISOString(),
    expiryAt: new Date(act.expiry).toISOString(),
    daysRemaining: Math.floor((act.expiry - Date.now()) / (24 * 60 * 60 * 1000))
  }));

  res.json({
    total: activationList.length,
    activations: activationList
  });
});

app.post('/admin/generate-sn', (req, res) => {
  const { tier = 'basic', count = 1 } = req.body;
  const generated = [];

  for (let i = 0; i < count; i++) {
    const sn = `SN-${tier.toUpperCase()}-${Date.now()}-${Math.random().toString(36).substr(2, 5).toUpperCase()}`;
    serialNumbers.set(sn, { used: false, tier });
    generated.push(sn);
  }

  console.log(`[ADMIN] Generated ${count} Serial Numbers (${tier})`);

  res.json({
    success: true,
    generated: generated
  });
});

// Start
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log('='.repeat(60));
  console.log('IntiKasir Mock Activation Server (ENCRYPTED)');
  console.log('='.repeat(60));
  console.log(`Server: http://localhost:${PORT}`);
  console.log('');
  console.log('Endpoints:');
  console.log(`  POST   /api/activate (encrypted)`);
  console.log(`  GET    /api/health`);
  console.log(`  GET    /admin/activations`);
  console.log(`  POST   /admin/generate-sn`);
  console.log('');
  console.log('Demo Serial Numbers:');
  serialNumbers.forEach((info, sn) => {
    console.log(`  - ${sn} (${info.tier}) ${info.used ? '[USED]' : '[AVAILABLE]'}`);
  });
  console.log('='.repeat(60));
});

