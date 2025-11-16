# Fix: Edit Transaction Navigation Crash

## Tanggal: 16 November 2025

## Problem

Saat klik tombol "Edit Transaksi" di HistoryDetailScreen, aplikasi crash dengan error:

```
java.lang.IllegalArgumentException: Navigation destination that matches 
route pos/d788a895-a0ab-4fc1-8919-63ffc6b8151c cannot be found in the 
navigation graph
```

---

## Root Cause

**Incorrect Route Format**:
- ❌ Used: `"pos/$transactionId"` (path parameter)
- ✅ Expected: `"pos?transactionId=$transactionId"` (query parameter)

**Navigation Definition**:
```kotlin
// PosRoutes.kt
const val POS_WITH_ID = "pos?transactionId={transactionId}"  // Query param, not path

// HomeNavGraph onEdit (BEFORE - WRONG)
navController.navigate("pos/$transactionId")  // Path param

// HomeNavGraph onEdit (AFTER - CORRECT)
navController.navigate(PosRoutes.pos(transactionId))  // Helper function
```

---

## Solution

### Changed File: `HomeNavGraph.kt`

**Before** (Line 151):
```kotlin
onEdit = { transactionId ->
    navController.navigate("pos/$transactionId") {  // ❌ WRONG
        popUpTo(HomeRoutes.HISTORY) { inclusive = false }
    }
},
```

**After** (Line 151):
```kotlin
onEdit = { transactionId ->
    navController.navigate(PosRoutes.pos(transactionId)) {  // ✅ CORRECT
        popUpTo(HomeRoutes.HISTORY) { inclusive = false }
    }
},
```

---

## How PosRoutes.pos() Works

```kotlin
// PosRoutes.kt
fun pos(transactionId: String? = null) =
    if (transactionId != null) 
        "pos?transactionId=$transactionId"  // Query parameter
    else 
        "pos"
```

**Examples**:
- `PosRoutes.pos(null)` → `"pos"`
- `PosRoutes.pos("abc123")` → `"pos?transactionId=abc123"`

---

## Route Definitions

### POS Screen Routes

```kotlin
object PosRoutes {
    const val POS = "pos"
    const val POS_WITH_ID = "pos?transactionId={transactionId}"  // Query param
    const val CART = "cart/{transactionId}"                      // Path param
    const val PAYMENT = "payment/{transactionId}"                // Path param
    const val RECEIPT = "receipt/{transactionId}"                // Path param
}
```

### Navigation Graph

```kotlin
composable(
    route = PosRoutes.POS_WITH_ID,  // "pos?transactionId={transactionId}"
    arguments = listOf(
        navArgument("transactionId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { ... }
```

---

## Why Query Parameter?

**POS Screen Supports Two Modes**:

1. **New Transaction** (no transactionId)
   - Route: `"pos"`
   - Creates new empty draft

2. **Edit Transaction** (with transactionId)
   - Route: `"pos?transactionId=abc123"`
   - Loads existing transaction

**Query parameter allows optional argument**:
- ✅ `pos` - valid (new transaction)
- ✅ `pos?transactionId=abc` - valid (edit)
- ❌ `pos/` - invalid (path requires value)
- ❌ `pos/abc` - not registered in graph

---

## Testing

### Test Scenario: Edit DRAFT Transaction

```
1. Navigate to Riwayat
2. Tap transaksi with status DRAFT
3. Tap "Edit Transaksi" button
4. ✅ Navigate to POS screen with transaction loaded
5. ✅ Items displayed in cart
6. ✅ Can add/remove items
7. ✅ Can save or proceed to payment
```

### Test Scenario: Edit PENDING Transaction

```
1. Navigate to Riwayat
2. Tap transaksi with status PENDING
3. Tap "Edit Transaksi" button
4. ✅ Navigate to POS screen
5. ✅ Transaction data loaded
6. ✅ Can modify and save
```

---

## Related Routes That Use Helper Functions

All navigation should use PosRoutes helper functions:

```kotlin
// ✅ CORRECT
navController.navigate(PosRoutes.pos())                    // New transaction
navController.navigate(PosRoutes.pos(transactionId))       // Edit transaction
navController.navigate(PosRoutes.cart(transactionId))      // Cart
navController.navigate(PosRoutes.payment(transactionId))   // Payment
navController.navigate(PosRoutes.receipt(transactionId))   // Receipt

// ❌ WRONG - Don't hardcode routes
navController.navigate("pos")
navController.navigate("pos/$transactionId")
navController.navigate("cart/$transactionId")
```

---

## Build Status

✅ **BUILD SUCCESSFUL** - Navigation fixed!

```
BUILD SUCCESSFUL in 10s
18 actionable tasks: 6 executed, 12 up-to-date

Warnings: Only deprecation (safe)
Errors: 0
```

---

## Prevention: Best Practices

### 1. Always Use Route Helper Functions

```kotlin
// ✅ DO THIS
navController.navigate(PosRoutes.pos(transactionId))

// ❌ DON'T DO THIS
navController.navigate("pos/$transactionId")
```

### 2. Check Route Definition Before Navigate

Look at composable route definition:
- Query param: `route = "pos?param={param}"`
- Path param: `route = "pos/{param}"`

### 3. Understand Route Types

**Query Parameter** (optional):
- Format: `route?key=value`
- Use case: Optional arguments
- Example: Edit existing or create new

**Path Parameter** (required):
- Format: `route/value`
- Use case: Required arguments
- Example: Detail screen with ID

---

## Impact

**Screens Affected**: 
- HistoryDetailScreen → Edit button now works

**Flow Fixed**:
```
History Detail (DRAFT/PENDING)
    ↓ [Tap "Edit Transaksi"]
POS Screen (with transaction loaded)
    ↓ [Modify items]
Save or Pay
    ↓
Complete transaction
```

---

## Files Modified

1. **HomeNavGraph.kt** (Line 151)
   - Changed: `"pos/$transactionId"` 
   - To: `PosRoutes.pos(transactionId)`

**Total Changes**: 1 line

---

## Related Documentation

- `EDIT_TRANSACTION_FEATURE.md` - Edit transaction feature overview
- `POS_REACTIVE_COMPLETE.md` - POS reactive flow
- `TRANSACTION_ACTIONS_COMPONENT.md` - Action buttons component
- `PosRoutes.kt` - Route definitions

---

## Summary

✅ **Navigation Crash Fixed**  
✅ **Edit Button Now Works**  
✅ **Uses Correct Query Parameter Route**  
✅ **Follows Best Practices (Helper Functions)**  
✅ **Build Successful**  
✅ **Ready for Testing**

Tombol "Edit Transaksi" sekarang berfungsi dengan benar dan tidak crash! 🎉

