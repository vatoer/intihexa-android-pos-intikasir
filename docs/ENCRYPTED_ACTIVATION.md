# 🔐 Encrypted Activation System - Updated Implementation

## Perubahan Format API

### ❌ Format Lama (OLD)
```json
// Request
{
  "serialNumber": "SN-DEMO-00001",
  "deviceId": "abc123def456"
}

// Response
{
  "success": true,
  "message": "Aktivasi berhasil",
  "signature": "BASE64_SIGNATURE",
  "expiry": 1735689600000
}
```

### ✅ Format Baru (NEW - Encrypted)
```json
// Request
{
  "cipher": "BASE64_RSA_ENCRYPTED_JSON"
}

// Cipher adalah enkripsi dari:
// { "sn": "SN-DEMO-00001", "device_uuid": "abc123def456" }

// Response
{
  "ok": true,
  "payload": "BASE64_ENCODED_JSON",
  "signature": "BASE64_SIGNATURE",
  "message": "Aktivasi berhasil"
}

// Payload (setelah decode) berisi:
// { "sn": "...", "device_uuid": "...", "expiry": 1735689600000, "tier": "basic" }
```

---

## Flow Enkripsi

### Client Side (Android)

```
1. Prepare payload
   { sn: "SN-XXX", device_uuid: "device123" }
   
2. Convert to JSON string
   '{"sn":"SN-XXX","device_uuid":"device123"}'
   
3. Encrypt with RSA-OAEP + SHA-256 using PUBLIC KEY
   cipher = RsaEncryption.encrypt(jsonString)
   
4. Send request
   POST /api/activate
   { "cipher": cipher_base64 }
   
5. Receive response
   { ok: true, payload: "...", signature: "..." }
   
6. Decode payload
   payloadJson = base64Decode(payload)
   
7. Verify signature
   isValid = SignatureVerifier.verify(payloadJson, signature)
   
8. If valid, parse and save
   data = JSON.parse(payloadJson)
   save to EncryptedSharedPreferences
```

### Server Side

```
1. Receive encrypted request
   { cipher: "BASE64_ENCRYPTED" }
   
2. Decrypt with PRIVATE KEY
   plaintext = rsaPrivateDecrypt(cipher)
   data = JSON.parse(plaintext)
   // { sn: "...", device_uuid: "..." }
   
3. Validate SN, check database, etc
   
4. Create response payload
   payload = { sn, device_uuid, expiry, tier }
   payloadJson = JSON.stringify(payload)
   
5. Sign payload with PRIVATE KEY
   signature = rsaSign(payloadJson)
   
6. Encode payload
   payloadBase64 = base64Encode(payloadJson)
   
7. Send response
   { ok: true, payload: payloadBase64, signature }
```

---

## Keuntungan Format Baru

### 🔒 Security
- ✅ Request data encrypted (tidak bisa dibaca di network)
- ✅ Response signed (tidak bisa dimodifikasi)
- ✅ Man-in-the-middle protection
- ✅ Replay attack protection

### 📦 Data Privacy
- ✅ Serial Number tidak terlihat di network
- ✅ Device UUID tidak terlihat di network
- ✅ Hanya server yang bisa decrypt

### ✅ Integrity
- ✅ Signature memastikan data dari server asli
- ✅ Tidak bisa dipalsukan

---

## Testing dengan Mock Server

### 1. Start Mock Server

```bash
cd docs
node mock-activation-server-encrypted.js
```

**IMPORTANT**: Server akan generate RSA key pair baru setiap start. Copy public key yang ditampilkan!

### 2. Update Public Key di Android

Copy public key dari console output, kemudian update di:

**File**: `app/src/main/java/id/stargan/intikasir/data/security/RsaEncryption.kt`

```kotlin
private const val PUBLIC_KEY_BASE64 = """
[PASTE_PUBLIC_KEY_HERE]
"""
```

**JUGA** update di `SignatureVerifier.kt` dengan public key yang sama!

### 3. Build & Test

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Test Aktivasi

- Input SN: `SN-DEMO-00001`
- Aktivasi akan berhasil!

---

## Production Setup

### 1. Generate Production Keys

```bash
cd docs
chmod +x generate-keys.sh
./generate-keys.sh
```

### 2. Update Android App

**RsaEncryption.kt** dan **SignatureVerifier.kt**:
```kotlin
private const val PUBLIC_KEY_BASE64 = """
[YOUR_PRODUCTION_PUBLIC_KEY]
"""
```

### 3. Update Server

Copy `private_key.pem` to server:
```bash
cp activation-keys/private_key.pem server/keys/
```

### 4. Use Production Server

Gunakan `server-encrypted.js` untuk production:
```bash
cd server-example
node server-encrypted.js
```

---

## API Reference

### POST /api/activate

**Request:**
```json
{
  "cipher": "BASE64_RSA_OAEP_ENCRYPTED_JSON"
}
```

**Success Response:**
```json
{
  "ok": true,
  "payload": "BASE64_ENCODED_JSON",
  "signature": "BASE64_SIGNATURE", 
  "message": "Aktivasi berhasil! Aplikasi siap digunakan."
}
```

**Error Response:**
```json
{
  "ok": false,
  "error": "ERROR_CODE",
  "message": "Error message",
  "payload": null,
  "signature": null
}
```

**Error Codes:**
- `INVALID_REQUEST` - Cipher tidak dikirim
- `DECRYPTION_FAILED` - Gagal decrypt (wrong public key?)
- `INVALID_DATA` - Data tidak lengkap
- `INVALID_SERIAL` - Serial Number tidak valid
- `SERIAL_USED` - Serial Number sudah digunakan
- `DEVICE_ACTIVATED` - Device sudah diaktivasi
- `SERVER_ERROR` - Server error

---

## Troubleshooting

### ❌ "Decryption failed"

**Penyebab**: Public key di Android tidak match dengan private key di server

**Solusi**:
1. Generate key pair baru
2. Update public key di Android (RsaEncryption.kt DAN SignatureVerifier.kt)
3. Copy private key ke server
4. Rebuild aplikasi

### ❌ "Signature tidak valid"

**Penyebab**: Public key di SignatureVerifier.kt berbeda dengan yang di RsaEncryption.kt

**Solusi**: Pastikan kedua file menggunakan public key yang SAMA

### ❌ "Cipher harus diisi"

**Penyebab**: Request tidak dalam format baru

**Solusi**: Pastikan menggunakan ActivationRepository yang sudah diupdate

---

## Migration Guide

Jika sudah ada data dengan format lama:

### Option 1: Hard Migration
1. Clear semua aktivasi existing
2. Deploy format baru
3. User perlu aktivasi ulang

### Option 2: Soft Migration
1. Server support kedua format (check request format)
2. Gradually migrate users
3. Deprecated old format setelah semua migrate

Untuk aplikasi baru, langsung gunakan format baru!

---

## Testing Checklist

- [ ] Mock server berjalan
- [ ] Public key di Android match dengan server
- [ ] Build berhasil
- [ ] Test aktivasi dengan SN-DEMO-00001
- [ ] Check server logs - dekripsi berhasil
- [ ] Check Android - signature valid
- [ ] Data tersimpan di EncryptedSharedPreferences
- [ ] Restart app - aktivasi masih valid

---

## Files Changed

### Android
- ✅ `RsaEncryption.kt` - NEW file untuk enkripsi
- ✅ `ActivationResponse.kt` - Updated models
- ✅ `ActivationRepository.kt` - Updated logic
- ✅ `SignatureVerifier.kt` - Same (verification logic)

### Server
- ✅ `mock-activation-server-encrypted.js` - NEW mock server
- ✅ `server-encrypted.js` - NEW production server

### Documentation
- ✅ `ENCRYPTED_ACTIVATION.md` - This file

---

## Security Notes

### ✅ DO:
- Use different key pairs for dev/prod
- Rotate keys regularly
- Keep private key SECRET
- Monitor failed decryption attempts

### ❌ DON'T:
- Commit private key to git
- Reuse keys across environments
- Share private key
- Skip signature verification

---

**Format enkripsi sudah diimplementasikan dan siap digunakan!** 🔒✅

