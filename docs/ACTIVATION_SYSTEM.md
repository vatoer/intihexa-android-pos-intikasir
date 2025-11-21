# Sistem Aktivasi IntiKasir

## Arsitektur Keamanan

Sistem aktivasi menggunakan arsitektur client-server dengan keamanan berlapis:

1. **Device Identification**: SSAID (Android ID)
2. **Encrypted Storage**: EncryptedSharedPreferences dengan AES256-GCM
3. **Signature Verification**: RSA 2048-bit dengan SHA256
4. **Secure Communication**: HTTPS dengan Retrofit

## Client-side (Android)

### Komponen Utama

1. **SecurePreferences** - Encrypted storage untuk SN dan status aktivasi
2. **SignatureVerifier** - Verifikasi digital signature dari server
3. **ActivationRepository** - Business logic aktivasi
4. **ActivationViewModel** - UI state management
5. **ActivationScreen** - User interface

### Flow Aktivasi

```
1. User membuka app pertama kali
2. App menampilkan Device ID (SSAID)
3. User menghubungi admin untuk mendapatkan Serial Number
4. User input Serial Number di app
5. App mengirim request ke server: { serialNumber, deviceId }
6. Server memverifikasi dan mengembalikan signature
7. App verifikasi signature dengan public key
8. Jika valid, simpan ke EncryptedSharedPreferences
9. User dapat menggunakan aplikasi
```

### Pengecekan Aktivasi

Setiap kali user buka menu (kecuali Settings), app akan:
- Cek apakah sudah diaktivasi
- Cek apakah sudah expired
- Verifikasi ulang signature
- Jika gagal, redirect ke screen aktivasi

## Server-side Implementation

### Endpoint: POST /api/activate

**Request:**
```json
{
  "serialNumber": "SN-XXXXX-XXXXX",
  "deviceId": "android-device-id"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Aktivasi berhasil",
  "signature": "BASE64_ENCODED_SIGNATURE",
  "expiry": 1735689600000
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Serial Number tidak valid",
  "signature": null,
  "expiry": null
}
```

### Signature Generation (Node.js/TypeScript Example)

```typescript
import crypto from 'crypto';
import fs from 'fs';

// Load private key (keep this SECRET on server)
const privateKey = fs.readFileSync('./private_key.pem', 'utf8');

// Generate signature
function generateSignature(serialNumber: string, deviceId: string, expiry: number): string {
  const data = `${serialNumber}:${deviceId}:${expiry}`;
  const sign = crypto.createSign('SHA256');
  sign.update(data);
  sign.end();
  const signature = sign.sign(privateKey);
  return signature.toString('base64');
}

// API Handler
app.post('/api/activate', async (req, res) => {
  const { serialNumber, deviceId } = req.body;
  
  // Validate Serial Number (check database, etc.)
  const isValid = await validateSerialNumber(serialNumber);
  
  if (!isValid) {
    return res.json({
      success: false,
      message: 'Serial Number tidak valid atau sudah digunakan',
      signature: null,
      expiry: null
    });
  }
  
  // Check if device already activated
  const existingActivation = await checkDeviceActivation(deviceId);
  if (existingActivation) {
    return res.json({
      success: false,
      message: 'Device sudah diaktivasi dengan Serial Number lain',
      signature: null,
      expiry: null
    });
  }
  
  // Set expiry (contoh: 1 tahun dari sekarang)
  const expiry = Date.now() + (365 * 24 * 60 * 60 * 1000);
  
  // Generate signature
  const signature = generateSignature(serialNumber, deviceId, expiry);
  
  // Save to database
  await saveActivation(serialNumber, deviceId, expiry);
  
  return res.json({
    success: true,
    message: 'Aktivasi berhasil',
    signature: signature,
    expiry: expiry
  });
});
```

### Generate RSA Key Pair

```bash
# Generate private key (KEEP SECRET on server)
openssl genrsa -out private_key.pem 2048

# Generate public key (embed in Android app)
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Convert public key to Base64 for Android
cat public_key.pem | grep -v "BEGIN\|END" | tr -d '\n'
```

### Database Schema

```sql
CREATE TABLE activations (
  id SERIAL PRIMARY KEY,
  serial_number VARCHAR(255) UNIQUE NOT NULL,
  device_id VARCHAR(255) UNIQUE NOT NULL,
  expiry_timestamp BIGINT NOT NULL,
  activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_serial_number ON activations(serial_number);
CREATE INDEX idx_device_id ON activations(device_id);
```

## Setup

### 1. Generate Key Pair

```bash
cd server
openssl genrsa -out keys/private_key.pem 2048
openssl rsa -in keys/private_key.pem -pubout -out keys/public_key.pem
```

### 2. Update Android Public Key

Copy the public key content:
```bash
cat keys/public_key.pem | grep -v "BEGIN\|END" | tr -d '\n'
```

Paste ke `SignatureVerifier.kt`:
```kotlin
private const val PUBLIC_KEY_BASE64 = """
    [PASTE_YOUR_PUBLIC_KEY_HERE]
"""
```

### 3. Update Server URL

Di `ActivationModule.kt`, update BASE_URL:
```kotlin
private const val BASE_URL = "https://your-server.com/"
```

## Security Best Practices

1. **Never expose Private Key** - Keep it on server only
2. **Use HTTPS only** - Never use HTTP for activation
3. **Rate Limiting** - Limit activation attempts per IP/device
4. **Audit Logging** - Log all activation attempts
5. **Serial Number Rotation** - Generate unique SN per customer
6. **Device Binding** - One SN = One Device ID
7. **Expiry Management** - Set reasonable expiry dates
8. **Signature Validation** - Always verify on both sides

## Testing

### Mock Server Response (untuk development)

```json
{
  "success": true,
  "message": "Aktivasi berhasil (DEMO MODE)",
  "signature": "MOCK_SIGNATURE_FOR_TESTING",
  "expiry": 1767225600000
}
```

### Bypass Activation (Development Only)

Untuk development, bisa temporary bypass dengan:
```kotlin
// Di ActivationRepository.kt - REMOVE IN PRODUCTION
fun isActivated(): Boolean {
    if (BuildConfig.DEBUG) return true // DEVELOPMENT ONLY
    // ... existing validation code
}
```

## Troubleshooting

### Signature Verification Failed
- Pastikan public key di Android sama dengan pasangan private key di server
- Pastikan format data yang di-sign sama persis: `"${sn}:${deviceId}:${expiry}"`
- Pastikan encoding UTF-8

### EncryptedSharedPreferences Error
- Minimum Android API 23 (Marshmallow)
- Jika ada masalah, clear app data dan reinstall

### Network Error
- Pastikan server URL benar
- Pastikan HTTPS valid (certificate)
- Check firewall/proxy settings

## Monitoring

Metrics yang perlu dimonitor:
- Total aktivasi success/failed
- Device ID yang mencoba aktivasi berulang kali
- Serial Number yang invalid
- Response time activation endpoint
- Signature verification failure rate

## Future Enhancements

1. **Hardware Attestation** - Gunakan SafetyNet/Play Integrity API
2. **Certificate Pinning** - Pin SSL certificate di app
3. **Offline Grace Period** - Allow temporary offline usage
4. **Remote Deactivation** - Server can deactivate device
5. **License Transfer** - Allow transfer between devices
6. **Multi-tier Licensing** - Basic/Pro/Enterprise tiers
7. **Trial Mode** - 30-day trial tanpa aktivasi
8. **Auto-renewal** - Automatic license renewal

## Contact

Untuk pertanyaan teknis tentang implementasi aktivasi, hubungi tim development.

