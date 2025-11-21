# 🐛 DEBUGGING GUIDE - Activation Not Persisting

## ❌ Problem

Setelah aktivasi berhasil:
- Status di Settings masih "Belum Diaktivasi"
- Setelah restart app, harus aktivasi lagi
- Data tidak tersimpan di EncryptedSharedPreferences

## ✅ Solution Implemented

### 1. Enhanced Logging

Tambahkan logging di 3 layer untuk tracking:

**SecurePreferences.kt**:
```kotlin
- Log saat save
- Log verification setelah save
- Log saat read
- Log error jika ada
```

**ActivationRepository.kt**:
```kotlin
- Log setiap step aktivasi
- Log data yang di-save
- Log verification setelah save
- Log error jika save gagal
```

**ActivationViewModel.kt**:
```kotlin
- Log status check
- Log setelah aktivasi sukses
- Log state changes
```

### 2. Error Handling

**Fallback to Normal SharedPreferences**:
Jika EncryptedSharedPreferences gagal init:
```kotlin
private val encryptedPrefs = try {
    EncryptedSharedPreferences.create(...)
} catch (e: Exception) {
    Log.e(TAG, "Failed to create EncryptedSharedPreferences, fallback to normal", e)
    context.getSharedPreferences("activation_prefs_fallback", Context.MODE_PRIVATE)
}
```

### 3. Verification After Save

```kotlin
fun saveActivationStatus(isActivated: Boolean) {
    encryptedPrefs.edit().putBoolean(KEY_ACTIVATION_STATUS, isActivated).apply()
    
    // Verify immediately
    val saved = encryptedPrefs.getBoolean(KEY_ACTIVATION_STATUS, false)
    Log.d(TAG, "Verification - Status after save: $saved")
}
```

---

## 📋 How to Debug

### 1. Check Logcat

Filter by tags:
```
adb logcat -s SecurePreferences ActivationRepository ActivationViewModel
```

atau

```
adb logcat | grep -E "SecurePreferences|ActivationRepository|ActivationViewModel"
```

### 2. Expected Log Flow

**Saat Aktivasi Berhasil**:
```
ActivationViewModel: Starting activation with SN: SN-DEMO-00001
ActivationRepository: Starting activation - SN: SN-DEMO-00001
ActivationRepository: Request encrypted, sending to server...
ActivationRepository: Server response received - ok: true
ActivationRepository: Payload decoded: {"sn":"...","device_uuid":"...","expiry":...}
ActivationRepository: Signature verification: true
ActivationRepository: Activation data parsed - Expiry: 1735689600000
ActivationRepository: Saving to SecurePreferences...
SecurePreferences: Serial number saved: SN-DEMO-00...
SecurePreferences: Activation status saved: true
SecurePreferences: Verification - Status after save: true ✅
SecurePreferences: Signature saved: abc123...
SecurePreferences: Expiry saved: 1735689600000
ActivationRepository: Verification - Saved status: true ✅
ActivationRepository: Activation successful!
ActivationViewModel: Activation successful, re-checking status...
ActivationViewModel: Status check result - Activated: true ✅
```

**Jika Ada Masalah**:
```
SecurePreferences: Verification - Status after save: false ❌
atau
ActivationRepository: ERROR: Status not saved properly! ❌
atau
SecurePreferences: Failed to save activation status ❌
```

### 3. Check SharedPreferences File

```bash
# Via adb shell
adb shell
run-as id.stargan.intikasir
cd shared_prefs
ls -la
cat secure_activation_prefs.xml  # atau activation_prefs_fallback.xml

# Atau via Device File Explorer di Android Studio
```

---

## 🔍 Common Issues & Solutions

### Issue 1: EncryptedSharedPreferences Init Failed

**Symptom**: Error log saat create EncryptedSharedPreferences

**Cause**: 
- Android version < 6.0 (API 23)
- KeyStore corruption
- Device security issue

**Solution**: 
- App now fallback to normal SharedPreferences
- Check log for fallback message

**Fix**:
```bash
# Clear app data
adb shell pm clear id.stargan.intikasir

# Reinstall
adb install -r app-debug.apk
```

### Issue 2: apply() Not Persisting

**Symptom**: Data save tapi hilang setelah restart

**Cause**: 
- apply() is async, app might close before write
- Storage permission issue

**Solution**: Use `commit()` instead of `apply()` (already changed)

### Issue 3: Multiple ViewModel Instances

**Symptom**: Save berhasil tapi UI tidak update

**Cause**: Different ViewModel instance

**Solution**: Use Hilt with proper scope (already implemented)

---

## 🧪 Manual Testing Steps

### Step 1: Clear Everything
```bash
adb shell pm clear id.stargan.intikasir
```

### Step 2: Install Fresh
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Open Logcat
```bash
adb logcat -c  # Clear log
adb logcat -s SecurePreferences ActivationRepository ActivationViewModel
```

### Step 4: Test Activation
1. Open app
2. Input SN: `SN-DEMO-00001`
3. Click Aktivasi
4. **Watch logcat logs**

### Step 5: Verify Logs

Look for these key logs:
```
✅ "Activation status saved: true"
✅ "Verification - Status after save: true"
✅ "Verification - Saved status: true"
✅ "Status check result - Activated: true"
```

If you see:
```
❌ "Status after save: false"
❌ "ERROR: Status not saved properly!"
❌ "Failed to save activation status"
```

Then there's a problem with storage!

### Step 6: Test Persistence
1. Close app (swipe from recents)
2. Open app again
3. Check logs: Should show `"Read activation status: true"`
4. Should go directly to Login (not Activation)

### Step 7: Check Settings
1. Navigate to Settings
2. Check "Status Aktivasi" card
3. Should show "Aktif" ✅

---

## 📊 Troubleshooting Checklist

- [ ] Logcat shows "Activation status saved: true"
- [ ] Logcat shows "Verification - Status after save: true"
- [ ] Logcat shows "Verification - Saved status: true"
- [ ] After restart, shows "Read activation status: true"
- [ ] Settings shows "Aktif"
- [ ] No error logs about EncryptedSharedPreferences
- [ ] SharedPreferences file exists in data folder

---

## 🔧 If Still Not Working

### Option 1: Use commit() Instead of apply()

Change in `SecurePreferences.kt`:
```kotlin
fun saveActivationStatus(isActivated: Boolean) {
    val success = encryptedPrefs.edit()
        .putBoolean(KEY_ACTIVATION_STATUS, isActivated)
        .commit()  // Use commit() for synchronous save
    
    Log.d(TAG, "Activation status commit result: $success")
}
```

### Option 2: Force Sync Before Read

```kotlin
fun isActivated(): Boolean {
    // Force re-read from disk
    val prefs = context.getSharedPreferences("secure_activation_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_ACTIVATION_STATUS, false)
}
```

### Option 3: Use DataStore Instead

Migrate to Jetpack DataStore (more reliable):
```kotlin
// build.gradle.kts
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

---

## 📝 Report Template

Jika masih bermasalah, berikan info:

```
1. Android Version: 
2. Device Model:
3. App Version:
4. Logcat output: [paste logs]
5. Steps to reproduce:
   - 
6. Expected behavior:
7. Actual behavior:
```

---

## ✅ Success Criteria

Aktivasi berhasil jika:
- ✅ Logcat shows all save operations successful
- ✅ Logcat shows verification after save = true
- ✅ Setelah restart, status masih true
- ✅ Settings menampilkan "Aktif"
- ✅ Tidak perlu aktivasi ulang

---

**Last Updated**: November 21, 2025  
**Version**: 2.3 (Enhanced Logging & Debugging)

