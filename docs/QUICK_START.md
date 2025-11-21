# 🚀 Quick Start Guide - Sistem Aktivasi IntiKasir

## Untuk Testing (Development)

### 1. Setup Mock Server (5 menit)

```bash
# Masuk ke folder docs
cd docs

# Install dependencies
npm install express body-parser

# Jalankan mock server
node mock-activation-server.js
```

Server akan jalan di `http://localhost:3000`

Demo Serial Numbers yang tersedia:
- `SN-DEMO-00001`
- `SN-DEMO-00002`
- `SN-DEMO-00003`

### 2. Update Android App (2 menit)

Edit file: `app/src/main/java/id/stargan/intikasir/di/ActivationModule.kt`

Ubah BASE_URL ke localhost:
```kotlin
// Line 31
private const val BASE_URL = "http://10.0.2.2:3000/" // untuk emulator
// atau
private const val BASE_URL = "http://192.168.1.xxx:3000/" // untuk real device
```

### 3. Build & Install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Test Flow

1. **Buka app** → Muncul Activation Screen
2. **Lihat Device ID** (catat atau screenshot)
3. **Input Serial Number**: `SN-DEMO-00001`
4. **Klik Aktivasi**
5. **Tunggu** → Success!
6. **Restart app** → Langsung masuk (skip activation)
7. **Buka Settings** → Lihat status aktivasi

---

## Untuk Production

### 1. Generate Production Keys (10 menit)

```bash
cd docs
chmod +x generate-keys.sh
./generate-keys.sh
```

Catat output Base64 public key!

### 2. Update Android App

**File 1**: `app/src/main/java/id/stargan/intikasir/data/security/SignatureVerifier.kt`

Ganti public key (line 16-20):
```kotlin
private const val PUBLIC_KEY_BASE64 = """
[PASTE_YOUR_BASE64_PUBLIC_KEY_HERE]
"""
```

**File 2**: `app/src/main/java/id/stargan/intikasir/di/ActivationModule.kt`

Ganti BASE_URL (line 31):
```kotlin
private const val BASE_URL = "https://activation.yourdomain.com/"
```

### 3. Setup Production Server (30 menit)

```bash
# Clone/copy server example
cd docs/server-example

# Install dependencies
npm install

# Setup database
createdb intikasir_activation
psql intikasir_activation < schema.sql

# Copy private key
mkdir keys
cp ../activation-keys/private_key.pem keys/

# Configure environment
cp .env.example .env
nano .env  # Edit DB credentials

# Start server
npm start
```

### 4. Generate Serial Numbers

```bash
curl -X POST http://localhost:3000/admin/generate-sn \
  -H "Content-Type: application/json" \
  -d '{"tier": "basic", "count": 10}'
```

### 5. Build Production APK

```bash
# Build release APK
./gradlew assembleRelease

# Sign APK (jika belum)
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore your-keystore.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  your-alias

# Verify
jarsigner -verify -verbose -certs app-release.apk
```

---

## Troubleshooting Cepat

### ❌ "Gagal menghubungi server"
**Solusi**:
- Pastikan mock server jalan
- Untuk emulator gunakan `10.0.2.2` bukan `localhost`
- Untuk real device gunakan IP komputer
- Check firewall

### ❌ "Signature tidak valid"
**Solusi**:
- Pastikan public key di Android sama dengan private key di server
- Re-generate keys jika perlu
- Restart server setelah ganti key

### ❌ "Serial Number tidak valid"
**Solusi**:
- Gunakan Serial Number yang benar
- Check di server logs
- Generate SN baru jika perlu

### ❌ Build error "Unresolved reference security"
**Solusi**:
```bash
./gradlew --stop
./gradlew clean build
```

---

## Test Checklist

- [ ] Mock server berjalan
- [ ] App bisa compile
- [ ] Activation screen muncul
- [ ] Device ID ditampilkan
- [ ] Bisa input Serial Number
- [ ] Aktivasi berhasil
- [ ] Navigasi ke login
- [ ] Restart → skip activation
- [ ] Settings menampilkan status
- [ ] Dialog aktivasi di Settings works

---

## URLs Penting

### Development
- Mock Server: `http://localhost:3000`
- Health Check: `http://localhost:3000/api/health`
- Admin Panel: `http://localhost:3000/admin/activations`

### Production
- API: `https://activation.yourdomain.com/api/activate`
- Health: `https://activation.yourdomain.com/api/health`

---

## Support Commands

```bash
# Check mock server
curl http://localhost:3000/api/health

# List activations
curl http://localhost:3000/admin/activations

# Generate SN
curl -X POST http://localhost:3000/admin/generate-sn \
  -H "Content-Type: application/json" \
  -d '{"tier": "basic", "count": 1}'

# Test activation
curl -X POST http://localhost:3000/api/activate \
  -H "Content-Type: application/json" \
  -d '{"serialNumber": "SN-DEMO-00001", "deviceId": "test-device-123"}'
```

---

## Next Steps

Setelah testing berhasil:

1. ✅ Deploy server ke production dengan SSL
2. ✅ Update BASE_URL di app
3. ✅ Build production APK
4. ✅ Distribute to users
5. ✅ Monitor activation logs

---

**Total waktu setup**: ~15 menit untuk development, ~1 jam untuk production

**Support**: Lihat dokumentasi lengkap di folder `docs/`

