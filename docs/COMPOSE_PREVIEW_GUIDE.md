# Compose Preview Quick Guide

## What are Compose Previews?

Compose Previews allow you to see your UI components without running the entire app. They render directly in Android Studio, giving you instant feedback on design changes.

## How to View Previews

### Method 1: Split View
1. Open any component file with `@Preview` annotations
2. Click **"Split"** button in top-right corner
3. See code on left, previews on right

### Method 2: Design Tab
1. Open any component file with `@Preview` annotations
2. Click **"Design"** tab at top
3. See all preview variants in full screen

### Method 3: Interactive Preview
1. View preview in Split/Design mode
2. Click **"Interactive"** mode button
3. Interact with your component (click, scroll, etc.)

## Available Previews in History Module

### 📋 TransactionRow Previews
**File**: `TransactionRow.kt`

```kotlin
@Preview(name = "Transaction Row - Cash Payment")
@Preview(name = "Transaction Row - Large Amount")
```

**What you'll see**:
- Normal cash transaction (Rp 165.000)
- Large amount transaction (Rp 17.075.000)
- Different payment methods (CASH, QRIS)
- Different statuses (PAID, COMPLETED)

### 📦 ItemDetailRow Previews
**File**: `ItemDetailRow.kt`

```kotlin
@Preview(name = "Item Detail - No Discount")
@Preview(name = "Item Detail - With Discount")
@Preview(name = "Item Detail - Large Quantity")
```

**What you'll see**:
- Simple item without discount
- Item with per-unit discount breakdown
- Bulk item with large quantity

### 🔍 HistoryFilterBar Previews
**File**: `HistoryFilterBar.kt`

```kotlin
@Preview(name = "History Filter Bar - Expanded")
@Preview(name = "History Filter Bar - With Status Filter")
@Preview(name = "History Filter Bar - Custom Range")
```

**What you'll see**:
- All date range options
- Status filter chips
- Custom date range button
- Apply button

## Creating Your Own Previews

### Basic Preview
```kotlin
@Preview(name = "My Component", showBackground = true)
@Composable
private fun MyComponentPreview() {
    MaterialTheme {
        MyComponent(
            title = "Preview Title",
            onClick = {}
        )
    }
}
```

### Multiple Preview Variants
```kotlin
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Large Font", fontScale = 1.5f)
@Composable
private fun MyComponentPreviews() {
    MaterialTheme {
        MyComponent()
    }
}
```

### Preview Parameters (Advanced)
```kotlin
class MyDataProvider : PreviewParameterProvider<MyData> {
    override val values = sequenceOf(
        MyData(title = "Short"),
        MyData(title = "Very Long Title That Wraps"),
        MyData(title = "With Special Chars: ñ, é, ü")
    )
}

@Preview
@Composable
private fun MyComponentPreview(
    @PreviewParameter(MyDataProvider::class) data: MyData
) {
    MaterialTheme {
        MyComponent(data)
    }
}
```

## Preview Best Practices

### ✅ DO
- Create previews for all reusable components
- Show different states (empty, loading, error, success)
- Test edge cases (long text, large numbers)
- Use realistic data
- Wrap in MaterialTheme for consistent styling
- Use `showBackground = true` for visibility

### ❌ DON'T
- Connect to real database/network in previews
- Use actual ViewModels (use fake/mock data)
- Forget to make preview functions `private`
- Use production API keys or credentials

## Common Preview Annotations

### @Preview Parameters
```kotlin
@Preview(
    name = "Preview Name",              // Shows in preview panel
    showBackground = true,              // White background
    backgroundColor = 0xFFFFFFFF,       // Custom background color
    uiMode = UI_MODE_NIGHT_YES,        // Dark mode
    device = Devices.PIXEL_4,           // Specific device
    widthDp = 360,                      // Custom width
    heightDp = 640,                     // Custom height
    fontScale = 1.5f,                   // Font scaling
    locale = "id"                       // Indonesian locale
)
```

### Device Presets
```kotlin
@Preview(device = Devices.PIXEL_4)
@Preview(device = Devices.PIXEL_4_XL)
@Preview(device = Devices.PIXEL_C)  // Tablet
@Preview(device = Devices.AUTOMOTIVE_1024p)
@Preview(device = Devices.WEAR_OS_SMALL_ROUND)
```

## Troubleshooting

### Preview Not Showing?
1. **Build project**: Previews need compiled code
2. **Check for errors**: Fix any compilation errors
3. **Refresh**: Click refresh icon in preview panel
4. **Clear cache**: File → Invalidate Caches / Restart

### Preview Shows Error?
1. **Check imports**: Ensure all dependencies imported
2. **Avoid ViewModels**: Use fake data instead
3. **Check parameters**: Ensure all required params provided
4. **Check context**: Don't use context-dependent code

### Preview is Slow?
1. **Limit preview count**: Too many previews slow down rendering
2. **Simplify preview data**: Use minimal realistic data
3. **Disable live preview**: Only enable when needed
4. **Increase memory**: Android Studio → Preferences → Memory Settings

## Tips & Tricks

### 1. Preview Groups
```kotlin
@Preview(name = "Small", widthDp = 320)
@Preview(name = "Medium", widthDp = 360)
@Preview(name = "Large", widthDp = 400)
annotation class ComponentPreviews

@ComponentPreviews
@Composable
fun MyComponent() { }
```

### 2. Preview with Mock Data
```kotlin
object PreviewData {
    val sampleTransaction = TransactionEntity(
        id = "preview-1",
        transactionNumber = "INV-2024-0001",
        total = 150000.0,
        // ... other fields
    )
}

@Preview
@Composable
fun TransactionPreview() {
    TransactionRow(tx = PreviewData.sampleTransaction)
}
```

### 3. Conditional Preview Content
```kotlin
@Preview(name = "Empty State")
@Composable
fun ListPreview() {
    if (isPreview) {
        // Show mock data
        TransactionList(items = PreviewData.sampleTransactions)
    }
}

val isPreview: Boolean
    @Composable
    get() = LocalInspectionMode.current
```

## Resources

- [Official Compose Preview Guide](https://developer.android.com/jetpack/compose/tooling/previews)
- [Preview Parameter Providers](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

---
**Pro Tip**: Use previews during development to iterate quickly without rebuilding the app!

