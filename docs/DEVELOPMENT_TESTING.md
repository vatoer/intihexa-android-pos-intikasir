# 🛠️ Development Testing Guide - HTTP Cleartext

## Masalah: CLEARTEXT communication not permitted

Android secara default melarang komunikasi HTTP (hanya HTTPS yang diizinkan) untuk keamanan. Namun untuk development/testing di LAN lokal, kita perlu mengizinkan HTTP.

## ✅ Solusi yang Sudah Diterapkan

### 1. Network Security Configuration

File: `app/src/main/res/xml/network_security_config.xml`

```xml
<network-security-config>
    <!-- Development: Allow cleartext traffic for local testing -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.18.93</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>
    
    <!-- Production: Only HTTPS allowed -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 2. AndroidManifest.xml

Ditambahkan:
```xml
<!-- Internet permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Network security config -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## 🚀 Cara Testing dengan Development Server

### Setup 1: Jalankan Mock Server

Di komputer development (IP: 192.168.18.93):

```bash
cd /Volumes/X9/intihexa/Android/intihexa-android-pos-intikasir/docs
node mock-activation-server.js
```

Server akan jalan di `http://192.168.18.93:3000`

### Setup 2: Konfigurasi Sudah Benar ✅

File `ActivationModule.kt` sudah menggunakan IP yang benar:
```kotlin
private const val BASE_URL = "http://192.168.18.93:3000/"
```

### Setup 3: Build & Install

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install ke device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Setup 4: Test Aktivasi

1. **Buka aplikasi** di HP (real device)
2. **Lihat Activation Screen**
3. **Input Serial Number**: `SN-DEMO-00001`
4. **Klik Aktivasi**
5. **Check logs** di mock server - akan muncul request

---

## 📱 Testing Scenarios

### Scenario 1: Real Device di LAN yang sama

✅ **URL**: `http://192.168.18.93:3000/`  
✅ **Status**: Sudah dikonfigurasi dengan benar

**Pastikan:**
- HP dan komputer di WiFi yang sama
- Firewall tidak memblokir port 3000
- Server mock berjalan

**Test:**
```bash
# Di HP, buka browser test:
http://192.168.18.93:3000/api/health

# Seharusnya muncul:
{"status":"ok","timestamp":...}
```

### Scenario 2: Android Emulator

URL: `http://10.0.2.2:3000/`

Update `ActivationModule.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

`10.0.2.2` adalah IP spesial untuk mengakses localhost dari emulator.

### Scenario 3: Mixed Testing (Emulator + Real Device)

Buat 2 flavor atau gunakan BuildConfig:

File: `app/build.gradle.kts`
```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "ACTIVATION_URL", 
                "\"http://192.168.18.93:3000/\"")
        }
        release {
            buildConfigField("String", "ACTIVATION_URL", 
                "\"https://activation.yourdomain.com/\"")
        }
    }
}
```

Kemudian di `ActivationModule.kt`:
```kotlin
private const val BASE_URL = BuildConfig.ACTIVATION_URL
```

---

## 🔒 Security Notes

### ⚠️ PENTING: Hanya untuk Development!

Configuration ini **HANYA untuk development/testing**. Untuk production:

1. ✅ Gunakan HTTPS dengan SSL certificate
2. ✅ Hapus atau update `network_security_config.xml`
3. ✅ Update BASE_URL ke production URL

### Production Network Security Config

Untuk production, gunakan:

```xml
<network-security-config>
    <!-- Production: HTTPS only -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

## 🐛 Troubleshooting

### ❌ Masih "CLEARTEXT not permitted"

**Check:**
1. File `network_security_config.xml` sudah dibuat?
2. AndroidManifest.xml sudah reference file tersebut?
3. IP address di config sesuai dengan server?
4. Rebuild aplikasi (clean build)?

**Fix:**
```bash
./gradlew clean
./gradlew assembleDebug
adb uninstall id.stargan.intikasir
adb install app/build/outputs/apk/debug/app-debug.apk
```

### ❌ Connection timeout

**Check:**
1. Server mock sedang berjalan?
2. Firewall tidak blocking port 3000?
3. HP dan komputer di network yang sama?

**Test koneksi:**
```bash
# Di HP, install Termux dan test:
curl http://192.168.18.93:3000/api/health

# Atau buka di browser HP:
http://192.168.18.93:3000/api/health
```

### ❌ Unable to resolve host

**Check:**
1. IP address benar?
2. Network connectivity?
3. DNS issues?

**Test ping:**
```bash
# Di terminal komputer:
ping 192.168.18.93

# Di HP (Termux):
ping 192.168.18.93
```

---

## 📋 Testing Checklist

### Pre-testing
- [ ] Mock server berjalan di `192.168.18.93:3000`
- [ ] HP dan komputer di WiFi yang sama
- [ ] Firewall tidak blocking
- [ ] Browser test: `http://192.168.18.93:3000/api/health` works

### Build
- [ ] Clean build: `./gradlew clean`
- [ ] Assemble: `./gradlew assembleDebug`
- [ ] Install: `adb install -r app-debug.apk`

### Testing
- [ ] App terbuka
- [ ] Activation screen muncul
- [ ] Device ID ditampilkan
- [ ] Input SN: `SN-DEMO-00001`
- [ ] Klik Aktivasi
- [ ] Check server logs - request masuk
- [ ] Aktivasi berhasil
- [ ] Navigate ke login

### Verification
- [ ] Restart app - skip activation
- [ ] Settings - status aktif
- [ ] Check EncryptedSharedPreferences tersimpan

---

## 🌐 Network Configuration Summary

| Environment | URL | Notes |
|-------------|-----|-------|
| Development (Real Device) | `http://192.168.18.93:3000/` | ✅ Current config |
| Development (Emulator) | `http://10.0.2.2:3000/` | Need update |
| Production | `https://activation.yourdomain.com/` | Future |

---

## 📝 Quick Commands

```bash
# Start mock server
cd docs && node mock-activation-server.js

# Build & install
./gradlew clean assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk

# Check logs
adb logcat | grep -i activation

# Uninstall (fresh install)
adb uninstall id.stargan.intikasir
```

---

## ✅ Ready to Test!

Konfigurasi sudah lengkap. Anda bisa langsung:

1. Start mock server
2. Build & install app
3. Test activation dengan `SN-DEMO-00001`

Selamat testing! 🚀

