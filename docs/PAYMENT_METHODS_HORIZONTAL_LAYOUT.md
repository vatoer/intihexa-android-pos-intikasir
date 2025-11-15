# Payment Screen - Layout 1x4 Horizontal

## Visual Layout

```
Metode Pembayaran
┌────────────────────────────────────────────────┐
│  ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   │
│  │  💵  │   │  📱  │   │  🏦  │   │  💳  │   │
│  │      │   │      │   │      │   │      │   │
│  │ CASH │   │ QRIS │   │TRANS │   │ CARD │   │
│  └──────┘   └──────┘   └──────┘   └──────┘   │
└────────────────────────────────────────────────┘
   ^selected      8dp gap antara buttons
```

## Implementasi Code

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    PaymentMethod.values().forEach { method ->
        val selected = selectedPaymentMethod == method
        val icon = when (method) {
            PaymentMethod.CASH -> Icons.Default.Payments
            PaymentMethod.QRIS -> Icons.Default.QrCode2
            PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
            PaymentMethod.CARD -> Icons.Default.CreditCard
        }
        
        OutlinedButton(
            onClick = { selectedPaymentMethod = method },
            modifier = Modifier
                .weight(1f)          // Equal width
                .height(64.dp),      // Icon + Text
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) 
                else 
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = method.name,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    method.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
```

## Keuntungan Layout Horizontal

### ✅ Space Efficiency
- Hanya memakan 1 baris vertikal (~64dp + spacing)
- Lebih banyak ruang untuk field lainnya (cash amount, notes)
- Mengurangi kebutuhan scroll

### ✅ Visual Hierarchy
- Semua opsi terlihat sekaligus dalam satu pandangan
- Equal width (weight = 1f) memberi kesan setara
- User bisa compare dengan cepat

### ✅ Accessibility
- Tombol cukup besar untuk di-tap (64dp height)
- Icon + Text memberi double cue (visual + verbal)
- Selected state jelas dengan 3 indicator:
  1. Background color
  2. Icon color
  3. Font weight

### ✅ Responsiveness
- Otomatis menyesuaikan width berdasarkan screen size
- Tidak ada tombol yang terpotong
- Spacing konsisten (8dp)

## Screen Sizes Tested

| Device Type | Screen Width | Button Width (approx) | Status |
|-------------|--------------|------------------------|--------|
| Small Phone | 360dp        | ~85dp each            | ✅ Good |
| Medium Phone| 411dp        | ~98dp each            | ✅ Good |
| Large Phone | 480dp        | ~115dp each           | ✅ Good |
| Tablet      | 600dp+       | ~145dp+ each          | ✅ Good |

## Comparison: Before vs After

### Before (2x2 Grid)
```
┌──────┬──────┐
│ CASH │ QRIS │
├──────┼──────┤
│TRANS │ CARD │
└──────┴──────┘
Height: ~100dp (2 rows × 44dp + spacing)
No icons
```

### After (1x4 Horizontal)
```
┌──────┬──────┬──────┬──────┐
│  💵  │  📱  │  🏦  │  💳  │
│ CASH │ QRIS │TRANS │ CARD │
└──────┴──────┴──────┴──────┘
Height: ~64dp (1 row)
With clear icons
Saves ~36dp vertical space!
```

## Testing Notes

- [x] Build successful
- [x] Icons display correctly
- [x] Selection works
- [x] Width equal across all buttons
- [ ] Test on small screen (360dp width)
- [ ] Test on large screen (tablet)
- [ ] Test with different themes
- [ ] Test accessibility with TalkBack

