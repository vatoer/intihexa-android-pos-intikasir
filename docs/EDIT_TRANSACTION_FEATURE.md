# Fitur Edit Transaksi dari Riwayat

## Tanggal: 16 November 2025

## Deskripsi Fitur

Menambahkan kemampuan untuk mengedit transaksi yang masih berstatus **DRAFT** atau **PENDING** langsung dari halaman detail riwayat transaksi.

---

## Status Transaksi yang Dapat Diedit

### ✅ Dapat Diedit:
- **DRAFT** - Transaksi draft yang belum diselesaikan
- **PENDING** - Transaksi pesanan yang belum dibayar

### ❌ Tidak Dapat Diedit:
- **PAID** - Sudah dibayar
- **PROCESSING** - Sedang diproses
- **COMPLETED** - Selesai
- **CANCELLED** - Dibatalkan
- **REFUNDED** - Dikembalikan

**Alasan**: Transaksi yang sudah dalam proses atau selesai tidak boleh diubah untuk menjaga integritas data dan audit trail.

---

## Implementasi

### 1. UI Changes - HistoryDetailScreen

**Lokasi**: `HistoryScreens.kt`

**Perubahan**:
```kotlin
@Composable
fun HistoryDetailScreen(
    // ...existing parameters...
    onEdit: (String) -> Unit,  // NEW: Callback untuk edit
    // ...
) {
    // ...
    
    // Di section Actions
    if (tx.status == TransactionStatus.DRAFT || tx.status == TransactionStatus.PENDING) {
        Button(
            onClick = { onEdit(tx.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Edit Transaksi")
        }
    }
}
```

**Visual Layout**:
```
┌────────────────────────────────────┐
│  Detil Transaksi                   │
├────────────────────────────────────┤
│  INV-20231116-0001                 │
│  Status: DRAFT / PENDING           │
│  ...                               │
├────────────────────────────────────┤
│  Actions:                          │
│  ┌──────────────────────────────┐ │
│  │ ✏️ Edit Transaksi            │ │ ← NEW (Primary)
│  └──────────────────────────────┘ │
│  ┌────────────┬─────────────────┐ │
│  │ 🖨️ Cetak   │ 📤 Bagikan     │ │
│  └────────────┴─────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │ 🗑️ Hapus (Admin)             │ │
│  └──────────────────────────────┘ │
└────────────────────────────────────┘
```

---

### 2. Navigation Flow

**Lokasi**: `HomeNavGraph.kt`

**Flow**:
```
History List 
    ↓ (tap transaction)
History Detail (DRAFT/PENDING)
    ↓ (tap "Edit Transaksi")
POS Screen (with transaction ID)
    ↓ (edit items, update payment, etc)
Payment Screen
    ↓ (complete or save as draft)
Receipt Screen / Back to History
```

**Implementation**:
```kotlin
HistoryDetailScreen(
    // ...
    onEdit = { transactionId ->
        navController.navigate("pos/$transactionId") {
            popUpTo(HomeRoutes.HISTORY) { inclusive = false }
        }
    }
)
```

**Navigation Path**:
- `history/detail/{transactionId}` → `pos/{transactionId}`
- Back stack: History tetap ada, sehingga user bisa kembali

---

### 3. POS Screen Integration

POS Screen sudah support loading existing transaction via transaction ID:

```kotlin
PosScreenReactive(
    transactionId = transactionId,  // From navigation argument
    // ...
)
```

**Reactive Loading**:
- ViewModel auto-load transaction dan items
- Cart ter-populate dengan existing items
- User bisa tambah/kurangi items
- Update payment method
- Update discount
- Save sebagai draft atau complete

---

## User Flow Scenarios

### Scenario 1: Edit Draft Transaction
```
1. User buka Riwayat
2. Tap transaksi dengan status DRAFT
3. Button "Edit Transaksi" muncul
4. Tap "Edit Transaksi"
5. Navigate ke POS Screen dengan items ter-load
6. User tambah/hapus items
7. User update payment method
8. User klik "Simpan" → back to history
   OR klik "Bayar" → lanjut ke payment
```

### Scenario 2: Edit Pending Order
```
1. Customer buat pesanan → status PENDING
2. Kasir buka Riwayat
3. Tap pesanan PENDING
4. Button "Edit Transaksi" muncul
5. Tap "Edit Transaksi"
6. POS Screen terbuka dengan items pesanan
7. Kasir tambah item yang terlewat
8. Kasir lanjut ke pembayaran
9. Complete transaction → status COMPLETED
```

### Scenario 3: Cannot Edit Completed
```
1. User buka Riwayat
2. Tap transaksi dengan status COMPLETED
3. Button "Edit Transaksi" TIDAK muncul
4. Hanya ada: Cetak, Bagikan, Hapus (admin)
```

---

## Button Visibility Logic

| Status | Edit Button | Cetak | Bagikan | Hapus (Admin) |
|--------|-------------|-------|---------|---------------|
| DRAFT | ✅ | ✅ | ✅ | ✅ |
| PENDING | ✅ | ✅ | ✅ | ✅ |
| PAID | ❌ | ✅ | ✅ | ✅ |
| PROCESSING | ❌ | ✅ | ✅ | ✅ |
| COMPLETED | ❌ | ✅ | ✅ | ✅ |
| CANCELLED | ❌ | ✅ | ✅ | ✅ |
| REFUNDED | ❌ | ✅ | ✅ | ✅ |

---

## Technical Details

### State Management
```kotlin
// Transaction status checked in Composable
if (tx.status == TransactionStatus.DRAFT || tx.status == TransactionStatus.PENDING) {
    // Show edit button
}
```

### Navigation Arguments
```kotlin
// POS route supports optional transaction ID
route = "pos/{transactionId?}"

// Navigate with ID for editing
navController.navigate("pos/$transactionId")
```

### Transaction Loading
```kotlin
// PosViewModelReactive
fun loadTransaction(transactionId: String) {
    viewModelScope.launch {
        combine(
            repo.getTransactionById(transactionId),
            repo.getTransactionItems(transactionId)
        ) { tx, items -> 
            // Update UI state with existing data
        }
    }
}
```

---

## Security & Data Integrity

### ✅ Safeguards:
1. **Status Check**: Hanya DRAFT dan PENDING yang editable
2. **Stock Management**: 
   - Saat edit, stock sudah ter-deduct untuk PENDING
   - Update items akan adjust stock accordingly
3. **Audit Trail**: 
   - `updatedAt` timestamp updated
   - Original `createdAt` preserved
4. **Transaction Number**: Tetap sama, tidak berubah

### ⚠️ Important Notes:
- **DRAFT**: Stock belum ter-deduct
- **PENDING**: Stock sudah ter-deduct saat create
- Edit PENDING → Update stock delta (difference between old and new items)

---

## UI/UX Considerations

### Button Styling
```kotlin
Button(
    onClick = { onEdit(tx.id) },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary  // Primary color
    )
)
```

**Rationale**: 
- Primary button (filled) → primary action
- Full width → easy to tap
- Icon + Text → clear intent
- Positioned first → priority action

### Button Order (Top to Bottom):
1. **Edit Transaksi** (Primary - DRAFT/PENDING only)
2. **Cetak** & **Bagikan** (Secondary - always visible)
3. **Hapus** (Destructive - Admin only)

---

## Testing Checklist

### Test Case 1: Edit DRAFT
- [ ] Create DRAFT transaction
- [ ] Navigate to History → Detail
- [ ] ✓ "Edit Transaksi" button visible
- [ ] Tap Edit
- [ ] ✓ Navigate to POS
- [ ] ✓ Items loaded correctly
- [ ] Add 1 item
- [ ] Save
- [ ] ✓ Transaction updated
- [ ] ✓ Navigate back to History

### Test Case 2: Edit PENDING
- [ ] Create PENDING transaction
- [ ] Navigate to History → Detail
- [ ] ✓ "Edit Transaksi" button visible
- [ ] Tap Edit
- [ ] ✓ Navigate to POS
- [ ] ✓ Items loaded
- [ ] Remove 1 item
- [ ] ✓ Stock adjusted
- [ ] Complete payment
- [ ] ✓ Status → COMPLETED

### Test Case 3: Cannot Edit COMPLETED
- [ ] Create COMPLETED transaction
- [ ] Navigate to History → Detail
- [ ] ✓ "Edit Transaksi" button NOT visible
- [ ] ✓ Only Cetak, Bagikan, Hapus available

### Test Case 4: Navigation Back Stack
- [ ] Edit transaction from History
- [ ] Navigate to POS
- [ ] Press back button
- [ ] ✓ Return to History Detail (not Home)

---

## Known Limitations

### Current Implementation:
1. **Stock Adjustment**: 
   - Edit PENDING transaction may require manual stock verification
   - Future: Auto-calculate stock delta

2. **Concurrent Edits**: 
   - No locking mechanism
   - Last write wins
   - Future: Implement optimistic locking

3. **Change Tracking**:
   - No detailed change log
   - Only `updatedAt` timestamp
   - Future: Audit log for item changes

---

## Future Enhancements

1. **Change History Log**:
   ```
   Transaction INV-001
   - Created: 16 Nov 10:00
   - Edited: 16 Nov 10:15 (Added item X)
   - Edited: 16 Nov 10:30 (Removed item Y)
   - Completed: 16 Nov 10:45
   ```

2. **Version Control**:
   - Save snapshot before edit
   - Allow rollback to previous version

3. **Edit Reason**:
   - Require note when editing
   - Track who made changes

4. **Notifications**:
   - Alert when pending order edited
   - Toast: "Transaksi berhasil diupdate"

5. **Conflict Resolution**:
   - Detect concurrent edits
   - Show merge UI if needed

---

## Build Status
✅ **BUILD SUCCESSFUL** - Feature ready for testing!

---

## Related Documentation
- `POS_REACTIVE_COMPLETE.md` - POS flow with reactive transactions
- `HISTORY_FEATURES_COMPLETE.md` - Complete history features
- `TransactionEntity.kt` - Transaction status definitions

---

## Implementation Summary

**Files Modified**:
1. `HistoryScreens.kt` - Added Edit button UI
2. `HomeNavGraph.kt` - Added navigation to POS for editing

**Key Changes**:
- ✅ Edit button for DRAFT/PENDING
- ✅ Navigation to POS with transaction ID
- ✅ Conditional visibility based on status
- ✅ Primary button styling
- ✅ Icon + text for clarity

**No Breaking Changes**: Backward compatible with existing flows.

**Testing**: Ready for manual testing on device/emulator.

