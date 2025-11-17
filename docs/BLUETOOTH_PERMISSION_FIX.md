# Bluetooth Permission Fix & Improvements

## Tanggal: 17 November 2025

## Masalah yang Diperbaiki

### 1. SecurityException saat Test Print ESC/POS
**Error:** 
```
java.lang.SecurityException: Need android.permission.BLUETOOTH_SCAN permission
```

**Penyebab:**
- Pada Android 12+ (SDK 31+), `BLUETOOTH_CONNECT` dan `BLUETOOTH_SCAN` harus diminta sebagai runtime permissions
- Aplikasi memanggil `BluetoothAdapter.cancelDiscovery()` dan `adapter.bondedDevices` tanpa memeriksa izin terlebih dahulu

**Solusi:**
- Menambahkan runtime permission checks sebelum operasi Bluetooth
- Request permissions menggunakan `RequestMultiplePermissions` untuk kedua permission sekaligus
- Guard semua Bluetooth API calls dengan permission checks

---

## Perubahan yang Dilakukan

### 1. BluetoothPermissionHelper (Baru)
**File:** `/app/src/main/java/id/stargan/intikasir/util/BluetoothPermissionHelper.kt`

Helper object yang menyediakan:
- `hasBluetoothPermissions(context)` - Check apakah semua permission granted
- `getRequiredPermissions()` - Return array permissions yang perlu direquest
- `hasBluetoothScanPermission(context)` - Check BLUETOOTH_SCAN permission
- `hasBluetoothConnectPermission(context)` - Check BLUETOOTH_CONNECT permission

**Keuntungan:**
- Reusable di semua screen yang menggunakan Bluetooth
- Centralized logic untuk permission checks
- Mendukung backward compatibility (Android < 12 tidak perlu runtime permission)

### 2. ESCPosPrinter Improvements

**Perubahan:**
- Menambahkan sealed class `PrintResult` untuk error handling yang lebih baik:
  ```kotlin
  sealed class PrintResult {
      object Success : PrintResult()
      data class Error(val message: String) : PrintResult()
  }
  ```
- Return `PrintResult` dari `printReceipt()` dan `printQueueTicket()` instead of void
- Menambahkan logging (menggunakan Android Log) untuk debugging
- Menggunakan `BluetoothPermissionHelper` untuk permission checks
- Error handling yang lebih detail dengan pesan yang informatif
- Guard `adapter.cancelDiscovery()` dengan permission check

**Keuntungan:**
- User mendapat feedback yang jelas saat print gagal
- Developer bisa debug issue dengan lebih mudah melalui logs
- Tidak ada silent failures

### 3. StoreSettingsScreen Updates

**Perubahan:**
- Menggunakan `BluetoothPermissionHelper` untuk simplify permission logic
- Initial permission state check saat screen dimuat
- Test Print button menampilkan hasil via Snackbar (bukan Toast):
  - Success: "Perintah cetak berhasil dikirim" (short duration)
  - Error: "Gagal mencetak: [error message]" (long duration)
- Simplified permission request flow

**UI/UX Improvements:**
- Konsisten menggunakan Snackbar di seluruh screen
- Feedback yang lebih jelas untuk user
- Permission request hanya muncul saat dibutuhkan

### 4. HomeNavGraph & ReceiptPrinter Updates

**Perubahan:**
- Handle `PrintResult` dari `ESCPosPrinter.printReceipt()` dan `printQueueTicket()`
- Tampilkan Toast message untuk success/error saat print dari Receipt screen
- Improved error feedback untuk user

---

## Testing Checklist

### Untuk Android 12+ (SDK 31+):
- [x] App requests BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions
- [x] Permission denial handled gracefully (user-friendly message)
- [x] Test Print works after granting permissions
- [x] Bonded devices list appears after granting permissions
- [x] Print success shows success message
- [x] Print failure shows error message with details

### Untuk Android < 12:
- [x] No runtime permission needed (auto-granted at install)
- [x] Test Print works immediately
- [x] Bonded devices list appears immediately

### Edge Cases:
- [x] Bluetooth disabled → Shows "Nyalakan Bluetooth terlebih dahulu"
- [x] No printer configured → Shows "Printer belum dikonfigurasi"
- [x] Bluetooth unavailable → Shows "Bluetooth tidak tersedia"
- [x] Connection failed → Shows detailed error message

---

## Best Practices yang Diterapkan

1. **Separation of Concerns**
   - Permission logic di helper class terpisah
   - Print logic di ESCPosPrinter
   - UI logic di Composable screens

2. **Error Handling**
   - Sealed class untuk type-safe result
   - Detailed error messages
   - Logging for debugging

3. **User Experience**
   - Clear feedback (Snackbar/Toast)
   - Permission request only when needed
   - User-friendly error messages in Indonesian

4. **Backward Compatibility**
   - Works on Android < 12 without changes
   - Graceful degradation

5. **Code Reusability**
   - BluetoothPermissionHelper dapat digunakan di screen lain
   - PrintResult pattern dapat diterapkan ke fitur lain

---

## Files Modified

1. ✅ **NEW:** `util/BluetoothPermissionHelper.kt`
2. ✅ `feature/pos/print/ESCPosPrinter.kt`
3. ✅ `feature/settings/ui/StoreSettingsScreen.kt`
4. ✅ `feature/home/navigation/HomeNavGraph.kt`
5. ✅ `feature/pos/print/ReceiptPrinter.kt`

---

## Next Improvements (Optional)

1. **Background Print Queue**
   - Implement print queue untuk handle multiple print jobs
   - Retry mechanism untuk failed prints

2. **Printer Auto-Discovery**
   - Scan untuk printer Bluetooth nearby
   - Auto-select printer berdasarkan criteria

3. **Print Preview**
   - Show preview before print
   - Confirmation dialog

4. **Print History/Logs**
   - Track successful/failed prints
   - Analytics untuk troubleshooting

5. **Settings Validation**
   - Test printer connection before saving
   - Validate printer compatibility

---

## Catatan untuk Developer

- **Log Tag:** Gunakan tag `"ESCPosPrinter"` untuk filter logs terkait printing
- **Permission Request:** Selalu check permission sebelum Bluetooth operation
- **Error Messages:** Gunakan bahasa Indonesia yang user-friendly
- **Testing:** Test di device dengan Bluetooth real, emulator tidak mendukung Bluetooth dengan baik

