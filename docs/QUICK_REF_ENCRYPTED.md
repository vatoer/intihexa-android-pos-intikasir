# 🚀 QUICK REFERENCE - Encrypted Activation

## 📝 Testing in 3 Steps

### Step 1: Start Mock Server
```bash
cd docs
node mock-activation-server-encrypted.js
```
→ Copy public key yang ditampilkan!

### Step 2: Update Public Key di Android

Update di **2 FILE** (harus SAMA!):

**File 1**: `RsaEncryption.kt` (line 11-16)
**File 2**: `SignatureVerifier.kt` (line 16-21)

```kotlin
private const val PUBLIC_KEY_BASE64 = """
[PASTE_PUBLIC_KEY_HERE]
"""
```

### Step 3: Build & Test
```bash
./gradlew clean assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Test dengan SN: `SN-DEMO-00001` ✅

---

## 📊 Format API

### Request
```json
{ "cipher": "BASE64_ENCRYPTED" }
```

### Response (Success)
```json
{
  "ok": true,
  "payload": "BASE64",
  "signature": "BASE64",
  "message": "Aktivasi berhasil"
}
```

### Response (Error)
```json
{
  "ok": false,
  "error": "ERROR_CODE",
  "message": "Error message"
}
```

---

## 🔑 Key Files

| File | Purpose |
|------|---------|
| `RsaEncryption.kt` | Encrypt request |
| `SignatureVerifier.kt` | Verify response |
| `ActivationRepository.kt` | Main logic |
| `mock-activation-server-encrypted.js` | Test server |

---

## ⚠️ Important Notes

1. **Public key** harus SAMA di RsaEncryption.kt DAN SignatureVerifier.kt
2. **Mock server** generate new keys setiap start
3. **Production** gunakan fixed keys dari generate-keys.sh
4. **Network** pastikan HTTP cleartext enabled untuk dev (sudah setup)

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| Decryption failed | Update public key di Android |
| Signature invalid | Public key berbeda di 2 file |
| Connection refused | Server belum jalan |
| Cleartext not permitted | Network security config issue |

---

## 📚 Docs

- `ENCRYPTED_ACTIVATION.md` - Complete guide
- `ENCRYPTED_IMPLEMENTATION_COMPLETE.md` - Summary
- `DEVELOPMENT_TESTING.md` - HTTP setup

---

## ✅ Checklist

- [ ] Mock server running
- [ ] Public key copied
- [ ] RsaEncryption.kt updated
- [ ] SignatureVerifier.kt updated  
- [ ] Build successful
- [ ] Test activation → SUCCESS!

---

**Version**: 2.0 Encrypted  
**Status**: READY ✅

