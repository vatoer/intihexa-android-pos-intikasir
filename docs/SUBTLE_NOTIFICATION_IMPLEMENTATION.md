# Implementasi Subtle Notification - Feedback yang Tidak Mengganggu

## Masalah

1. **Toast di HomeNavGraph** - Muncul di posisi default (bawah), menutupi tombol
2. **Snackbar di ReceiptScreen** - Warna hitam kontras, sangat mengalihkan perhatian

### User Complaint
> "Snackbar dan toast cukup mengganggu, warnanya hitam jadi terdistraksi, menutupi tombol aksi"

## Solusi: SubtleNotification Component

Membuat komponen notifikasi custom yang:
- ✅ **Tidak menutupi konten** - Slide in dari atas
- ✅ **Warna soft** - Menggunakan Material Theme container colors (95% opacity)
- ✅ **Tidak blocking** - User tetap bisa interact dengan UI
- ✅ **Auto-dismiss** - Hilang otomatis setelah 2 detik
- ✅ **Smooth animation** - Slide in/out dengan easing
- ✅ **Consistent** - Sama di seluruh aplikasi

## Implementation

### 1. SubtleNotification Component

**File**: `ui/common/components/SubtleNotification.kt`

```kotlin
@Composable
fun SubtleNotification(
    message: String,
    icon: ImageVector = Icons.Default.CheckCircle,
    type: NotificationType = NotificationType.Success
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
        tonalElevation = 2.dp
    ) {
        Row(padding = 12dp) {
            Icon(icon, size = 20.dp)
            Text(message, style = bodyMedium)
        }
    }
}
```

**Features**:
- Material 3 theming (automatic dark mode)
- 95% opacity untuk soft appearance
- Rounded corners 12dp
- Icon + Text layout
- Three types: Success, Error, Info

### 2. SubtleNotificationHost

```kotlin
@Composable
fun BoxScope.SubtleNotificationHost(
    state: SubtleNotificationState
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp)
    ) {
        SubtleNotification(...)
    }
}
```

**Animation**:
- Slide in dari atas (smooth)
- Fade in/out untuk transisi halus
- Duration: 300ms (fast but not jarring)
- Easing: FastOutSlowInEasing

### 3. Usage in ReceiptScreen

**SEBELUM (Mengganggu)**:
```kotlin
Scaffold(
    snackbarHost = {
        SnackbarHost(snackbarHostState)  // Default black, distracting
    }
)
```

**SESUDAH (Subtle)**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    Column { /* content */ }
    
    // Subtle notification at top
    SubtleNotificationHost(state = notificationState)
}

// Usage
scope.launch {
    notificationState.show(
        message = "Transaksi telah diselesaikan",
        icon = Icons.Default.CheckCircle,
        type = NotificationType.Success,
        duration = 2000L
    )
}
```

### 4. Callback Pattern for Print Feedback

**Update ReceiptScreen signature**:
```kotlin
fun ReceiptScreen(
    // ...
    onPrint: (onResult: (Boolean, String) -> Unit) -> Unit,
    onPrintQueue: (onResult: (Boolean, String) -> Unit) -> Unit,
    // ...
)
```

**Usage in ReceiptScreen**:
```kotlin
Button(onClick = {
    onPrint { success, message ->
        scope.launch {
            notificationState.show(
                message = message,
                icon = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                type = if (success) NotificationType.Success else NotificationType.Error
            )
        }
    }
})
```

**HomeNavGraph implementation**:
```kotlin
onPrint = { onResult ->
    scope.launch {
        val result = ReceiptPrinter.printReceiptOrPdf(...)
        result?.let { 
            when (it) {
                is Success -> onResult(true, "Struk berhasil dicetak")
                is Error -> onResult(false, "Gagal: ${it.message}")
            }
        } ?: onResult(true, "Struk berhasil dibuat")
    }
}
```

## Visual Comparison

### SEBELUM
```
┌─────────────────────────┐
│ TopAppBar              │
│ Success Info           │
│ Receipt Details        │
│ [Selesai]             │
│ [Cetak] [Bagikan]     │ ← Tertutup Toast
└─────────────────────────┘
  ┌─────────────────────┐
  │ ■ Transaksi Selesai│  ← Black Snackbar (distracting)
  └─────────────────────┘
```

### SESUDAH
```
┌─────────────────────────┐
│ TopAppBar              │
┌────────────────────┐    ← Subtle notification (soft color)
│ ✓ Transaksi Selesai│    ← Slides in smoothly
└────────────────────┘
│ Success Info           │
│ Receipt Details        │
│ [Selesai]             │ ← Tidak tertutup
│ [Cetak] [Bagikan]     │ ← Tidak tertutup
└─────────────────────────┘
```

## Color Palette

### Success (Green-ish)
- Container: `primaryContainer` @ 95% opacity
- Content: `onPrimaryContainer`
- Example: Light mode = soft green, Dark mode = soft teal

### Error (Red-ish)
- Container: `errorContainer` @ 95% opacity
- Content: `onErrorContainer`
- Example: Light mode = soft red, Dark mode = soft pink

### Info (Blue-ish)
- Container: `secondaryContainer` @ 95% opacity
- Content: `onSecondaryContainer`
- Example: Light mode = soft blue, Dark mode = soft purple

## Benefits

| Aspect | Toast/Snackbar | SubtleNotification |
|--------|----------------|-------------------|
| **Positioning** | Blocks content ❌ | Top, no blocking ✅ |
| **Color** | Black/Dark ❌ | Theme-aware, soft ✅ |
| **Distraction** | High ❌ | Minimal ✅ |
| **Animation** | Abrupt ❌ | Smooth slide ✅ |
| **Opacity** | Solid ❌ | 95% subtle ✅ |
| **Interaction** | Blocks UI ❌ | Non-blocking ✅ |
| **Dark Mode** | Inconsistent ❌ | Auto-adapt ✅ |

## User Experience Improvements

1. **Non-Intrusive** 
   - Muncul di atas dengan animasi smooth
   - Tidak menutupi tombol aksi
   - Transparan 95% (bisa lihat di baliknya)

2. **Soft Visual**
   - Warna container sesuai tema (bukan hitam pekat)
   - Rounded corners untuk kesan modern
   - Elevation subtle (2dp)

3. **Quick Feedback**
   - Muncul instant saat aksi selesai
   - Auto-dismiss 2 detik (tidak perlu manual dismiss)
   - Tidak blocking user untuk aksi berikutnya

4. **Consistent**
   - Pattern sama di seluruh aplikasi
   - Theme-aware (light/dark mode)
   - Icon + message format standard

## Files Changed

1. ✅ **SubtleNotification.kt** (NEW) - Reusable notification component
2. ✅ **ReceiptScreen.kt** - Replace Snackbar with SubtleNotification
3. ✅ **HomeNavGraph.kt** - Use callback pattern instead of Toast

## Migration from Toast/Snackbar

### Old Pattern (Toast)
```kotlin
Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
```

### New Pattern (Subtle)
```kotlin
scope.launch {
    notificationState.show(
        message = "Success!",
        icon = Icons.Default.CheckCircle,
        type = NotificationType.Success
    )
}
```

### Old Pattern (Snackbar)
```kotlin
snackbarHostState.showSnackbar(
    message = "Success!",
    duration = SnackbarDuration.Short
)
```

### New Pattern (Subtle)
```kotlin
scope.launch {
    notificationState.show(message = "Success!")
}
```

## Testing Checklist

- [x] Build berhasil
- [ ] Test: Klik "Selesai" → notification muncul di atas (soft color)
- [ ] Test: Klik "Cetak" → notification success/error dengan warna sesuai
- [ ] Test: Notification tidak menutupi tombol
- [ ] Test: Auto-dismiss setelah 2 detik
- [ ] Test: Smooth slide in/out animation
- [ ] Test: Dark mode → warna otomatis adjust
- [ ] Test: Multiple notification → queue properly

## Future Enhancements

1. **Queue System** - Multiple notifications stack
2. **Swipe to Dismiss** - User bisa dismiss manual
3. **Action Button** - Optional action (undo, retry, etc)
4. **Sound/Haptic** - Optional feedback
5. **Position Options** - Top, Center, Bottom
6. **Custom Duration** - Per message basis

## Best Practices

### ✅ DO
- Use SubtleNotification untuk semua feedback
- Pilih type yang sesuai (Success/Error/Info)
- Gunakan icon yang relevan
- Keep message short (< 50 chars)
- Auto-dismiss untuk success (2s)
- Longer duration untuk error (3s)

### ❌ DON'T
- Jangan gunakan Toast lagi
- Jangan gunakan Snackbar hitam
- Jangan blocking UI dengan notification
- Jangan notification terlalu lama (> 5s)
- Jangan spam notification

---
**Status**: ✅ Implemented
**Date**: 20 November 2025
**Impact**: Significantly improved UX - non-intrusive, soft, theme-aware feedback
**Build**: Successful

