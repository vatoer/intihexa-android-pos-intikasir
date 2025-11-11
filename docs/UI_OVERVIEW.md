# 🎨 Inti Kasir - UI/UX Overview

## Main Screen: POS (Point of Sale)

### Layout Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│  Inti Kasir - POS                                          ☰ Menu   │ ← TopAppBar (Primary Color)
├──────────────────────────────────┬──────────────────────────────────┤
│                                  │                                  │
│  LEFT PANEL (70%)                │  RIGHT PANEL (30%)               │
│  Product Grid                    │  Shopping Cart                   │
│                                  │                                  │
│  ┌────────────────────────────┐ │  ┌────────────────────────────┐ │
│  │  🔍 Cari produk...      ✕  │ │  │  Keranjang (3 item)        │ │
│  └────────────────────────────┘ │  └────────────────────────────┘ │
│                                  │                                  │
│  Category Filters:               │  ┌────────────────────────────┐ │
│  [Semua] [🍔 Makanan]            │  │ Nasi Goreng          🗑️   │ │
│  [🥤 Minuman] [🍿 Snack]         │  │                            │ │
│                                  │  │   [-]  2  [+]   Rp 30,000  │ │
│  ┌────────┐ ┌────────┐ ┌──────┐ │  └────────────────────────────┘ │
│  │ Nasi   │ │ Mie    │ │ Es   │ │  ┌────────────────────────────┐ │
│  │ Goreng │ │ Goreng │ │ Teh  │ │  │ Es Teh                🗑️   │ │
│  │        │ │        │ │      │ │  │                            │ │
│  │ Rp     │ │ Rp     │ │ Rp   │ │  │   [-]  1  [+]   Rp 3,000   │ │
│  │ 15,000 │ │ 12,000 │ │ 3,000│ │  └────────────────────────────┘ │
│  │        │ │        │ │      │ │                                  │
│  │Stok: 20│ │Stok: 15│ │Stok:✓│ │                                  │
│  └────────┘ └────────┘ └──────┘ │                                  │
│  ┌────────┐ ┌────────┐          │  ─────────────────────────────── │
│  │ Kopi   │ │ Keripik│          │  ┌────────────────────────────┐ │
│  │        │ │        │          │  │  Subtotal    Rp 33,000     │ │
│  │ Rp     │ │ Rp     │          │  │  Pajak(10%)  Rp 3,300      │ │
│  │ 5,000  │ │ 8,000  │          │  │  ─────────────────────────  │ │
│  │        │ │        │          │  │  TOTAL       Rp 36,300     │ │
│  │Stok: 30│ │Stok: 50│          │  └────────────────────────────┘ │
│  └────────┘ └────────┘          │                                  │
│                                  │  ┌────────────────────────────┐ │
│                                  │  │    🛒  BAYAR               │ │← Checkout Button
│                                  │  └────────────────────────────┘ │
│                                  │                                  │
└──────────────────────────────────┴──────────────────────────────────┘
```

---

## Color Scheme (Material 3)

### Light Theme
```
Primary:       #6750A4 (Purple)
OnPrimary:     #FFFFFF (White)
Secondary:     #625B71 (Gray-Purple)
OnSecondary:   #FFFFFF (White)
Tertiary:      #7D5260 (Pink)
Error:         #B3261E (Red)
Background:    #FFFBFE (Off-white)
Surface:       #FFFBFE (Off-white)
SurfaceVariant:#E7E0EC (Light purple-gray)
```

---

## Component Breakdown

### 1. TopAppBar
```kotlin
TopAppBar(
    title = "Inti Kasir - POS"
    backgroundColor = Primary
    textColor = OnPrimary
    actions = [Menu Icon]
)
```

### 2. Search Bar
```kotlin
OutlinedTextField(
    placeholder = "Cari produk..."
    leadingIcon = 🔍 Search
    trailingIcon = ✕ Clear (when text exists)
    shape = RoundedCorner(12dp)
)
```

### 3. Category Filter Chips
```kotlin
FilterChip(
    label = "🍔 Makanan"
    selected = true/false
    onClick = { filter category }
)
```

### 4. Product Card
```kotlin
Card(
    elevation = 2dp
    shape = RoundedCorner(12dp)
    padding = 12dp
    content = [
        ProductName (titleMedium, bold)
        Price (titleLarge, primary color, bold)
        Stock (bodySmall, gray or red if low)
    ]
    onClick = { add to cart }
)
```

### 5. Cart Item Card
```kotlin
Card(
    elevation = 1dp
    padding = 12dp
    content = [
        Row [ProductName, Delete Icon]
        Row [
            Quantity Controls (-, number, +)
            Subtotal (titleMedium, bold, primary)
        ]
    ]
)
```

### 6. Cart Summary
```kotlin
Card(
    background = Surface
    padding = 16dp
    content = [
        "Subtotal"  "Rp 33,000"
        "Pajak(10%)" "Rp 3,300"
        Divider
        "TOTAL"     "Rp 36,300" (bold, large, primary)
    ]
)
```

### 7. Checkout Button
```kotlin
Button(
    text = "🛒 BAYAR"
    fullWidth = true
    height = 56dp
    shape = RoundedCorner(12dp)
    enabled = cartItems.isNotEmpty()
    backgroundColor = Primary
    textColor = OnPrimary
)
```

---

## Typography (Material 3)

```kotlin
Display Large:  57sp, Regular
Display Medium: 45sp, Regular
Display Small:  36sp, Regular

Headline Large: 32sp, Regular
Headline Medium:28sp, Regular
Headline Small: 24sp, Regular

Title Large:    22sp, Regular    ← Product Price
Title Medium:   16sp, Medium     ← Product Name, Cart Subtotal
Title Small:    14sp, Medium

Body Large:     16sp, Regular
Body Medium:    14sp, Regular    ← Cart Summary Labels
Body Small:     12sp, Regular    ← Stock Info

Label Large:    14sp, Medium     ← Button Text
Label Medium:   12sp, Medium
Label Small:    11sp, Medium
```

---

## Spacing & Sizing

### Padding & Margin
```kotlin
Extra Small:  4dp
Small:        8dp
Medium:       12dp
Default:      16dp
Large:        24dp
Extra Large:  32dp
```

### Component Sizes
```kotlin
Search Bar Height:     56dp
Product Card Width:    150dp (adaptive grid)
Product Card Height:   Auto
Cart Item Height:      Auto (min 80dp)
Button Height:         56dp
Icon Size:             24dp
Icon Button:           32dp
Elevation Card:        1-2dp
Corner Radius:         12dp
```

---

## Interactions & Animations

### Product Card
```
Idle:    elevation = 2dp
Hover:   elevation = 4dp (on desktop)
Press:   scale = 0.95, elevation = 1dp
Ripple:  Primary color with alpha
```

### Cart Quantity Controls
```
+/- Buttons:
  - Icon button (32dp)
  - Ripple effect
  - Debounce 300ms for rapid clicks
  
Number Display:
  - titleMedium
  - 12dp horizontal padding
  - Center aligned
```

### Delete Item
```
Icon:       Delete/Trash (24dp)
Color:      Error red
OnClick:    Confirm dialog (optional)
Animation:  Slide out + fade
```

### Checkout Button
```
Idle:     Full color
Disabled: 38% opacity, gray
OnClick:  Show payment dialog
Ripple:   White with alpha
```

---

## Responsive Behavior

### Tablet/Large Screen
```
Product Grid:  4-5 columns
Cart Width:    Fixed 400dp (not percentage)
Layout:        Still side-by-side
```

### Phone/Portrait
```
Product Grid:  2-3 columns
Cart:          Bottom sheet atau separate screen
Layout:        May switch to tabs
```

---

## Accessibility

### Touch Targets
```
Minimum:      48dp x 48dp
Buttons:      56dp height
Icon Buttons: 48dp x 48dp
```

### Contrast Ratios
```
Text on Background:     4.5:1 minimum
Large Text:             3:1 minimum
Interactive Elements:   3:1 minimum
```

### Content Descriptions
```kotlin
Icon("Search", contentDescription = "Cari produk")
Icon("Delete", contentDescription = "Hapus item")
Icon("Add", contentDescription = "Tambah jumlah")
Icon("Remove", contentDescription = "Kurangi jumlah")
```

---

## States & Feedback

### Empty Cart
```
Display:  Empty state illustration/icon
Message:  "Keranjang kosong"
Action:   "Pilih produk untuk memulai"
```

### Loading
```
Show:     CircularProgressIndicator
Where:    Center of product grid
Overlay:  Semi-transparent background
```

### Error
```
Display:  Snackbar at bottom
Duration: 4 seconds
Action:   "Retry" button (optional)
Color:    Error color
```

### Success (After Payment)
```
Dialog:   Success checkmark
Message:  "Pembayaran Berhasil"
Details:  Transaction number
Actions:  "Cetak Ulang", "OK"
```

---

## Future Screens (Planned)

### Login Screen
```
┌─────────────────────────────┐
│       INTI KASIR            │
│                             │
│    [Logo/Icon]              │
│                             │
│    Masukkan PIN:            │
│    [  ●  ●  ●  ●  ]         │
│                             │
│    ┌───┬───┬───┐            │
│    │ 1 │ 2 │ 3 │            │
│    ├───┼───┼───┤            │
│    │ 4 │ 5 │ 6 │            │
│    ├───┼───┼───┤            │
│    │ 7 │ 8 │ 9 │            │
│    ├───┼───┼───┤            │
│    │   │ 0 │ ← │            │
│    └───┴───┴───┘            │
└─────────────────────────────┘
```

### Payment Dialog
```
┌─────────────────────────────────┐
│  Pembayaran                  ✕  │
├─────────────────────────────────┤
│                                 │
│  Total: Rp 36,300               │
│                                 │
│  Metode Pembayaran:             │
│  ◉ Tunai                        │
│  ○ QRIS                         │
│  ○ Kartu                        │
│  ○ Transfer                     │
│                                 │
│  Jumlah Uang:                   │
│  ┌─────────────────────────┐   │
│  │  Rp 50,000             │   │
│  └─────────────────────────┘   │
│                                 │
│  Kembalian: Rp 13,700           │
│                                 │
│  ┌─────────────────────────┐   │
│  │   PROSES PEMBAYARAN     │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

---

## Icon Usage

### Material Icons Used
```
Menu:              menu
Search:            search
Clear:             clear
Delete:            delete
Add:               add
KeyboardArrowDown: keyboard_arrow_down
ShoppingCart:      shopping_cart
CheckCircle:       check_circle (success)
Error:             error (error state)
Print:             print
Settings:          settings
Person:            person
Logout:            logout
```

---

**Created:** November 11, 2025  
**Version:** 1.0  
**Status:** POS Screen Complete ✅

