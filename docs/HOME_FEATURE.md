# Home Feature - Menu Utama

## Overview
Fitur Home adalah halaman utama aplikasi IntiKasir yang menampilkan menu-menu utama dalam bentuk grid card yang interaktif dan modern.

## Features

### Menu Items
Terdapat 7 menu utama:

1. **Produk** 🛍️
   - Kelola produk (tambah, edit, hapus)
   - Route: `products`

2. **Riwayat** 📜
   - Lihat riwayat transaksi
   - Route: `history`

3. **Pengeluaran** 💸
   - Kelola pengeluaran operasional
   - Route: `expenses`

4. **Laporan** 📊
   - Lihat laporan keuangan dan statistik
   - Route: `reports`

5. **Cetak Resi** 🖨️
   - Cetak ulang resi transaksi
   - Route: `print_receipt`

6. **Kasir** 💰
   - Halaman kasir untuk transaksi
   - Route: `cashier`

7. **Pengaturan** ⚙️
   - Pengaturan aplikasi
   - Route: `settings`

## UI/UX Design

### Layout
- **Grid Layout**: 3 kolom per baris
- **Card Design**: Square cards dengan aspek rasio 1:1
- **Spacing**: 12dp antar card
- **Padding**: 16dp content padding

### Components
```
HomeScreen (Main)
├── TopAppBar
│   ├── Title: "IntiKasir"
│   └── Logout Button
├── Header Section
│   ├── "Menu Utama"
│   └── "Pilih menu untuk memulai"
└── LazyVerticalGrid
    └── MenuCard (x7)
        ├── Icon (48dp)
        ├── Title
        └── Description
```

### Material 3 Features
- ✅ **TopAppBar** dengan primaryContainer color
- ✅ **Card** dengan elevation dan ripple effect
- ✅ **Typography** menggunakan Material 3 type scale
- ✅ **Color Scheme** mengikuti theme
- ✅ **Icons** dari Material Icons Extended
- ✅ **AlertDialog** untuk konfirmasi logout

## Architecture

### Clean Architecture Layers

```
presentation/
├── ui/
│   ├── HomeScreen.kt
│   └── components/
│       └── MenuCard.kt
└── navigation/
    ├── HomeRoutes.kt
    └── HomeNavGraph.kt

domain/
└── model/
    └── MenuItem.kt
```

### File Structure

#### 1. **HomeScreen.kt**
Main screen dengan:
- Scaffold layout
- TopAppBar dengan logout button
- LazyVerticalGrid untuk menu items
- AlertDialog untuk konfirmasi logout

#### 2. **MenuCard.kt**
Reusable component untuk menu item:
- Card clickable dengan ripple effect
- Icon, Title, dan Description
- Square aspect ratio (1:1)
- Material 3 styling

#### 3. **MenuItem.kt**
Domain model untuk menu:
- Data class MenuItem
- Object MenuItems dengan list menu

#### 4. **HomeRoutes.kt**
Constants untuk navigation routes

#### 5. **HomeNavGraph.kt**
Navigation graph dengan:
- Home screen route
- Placeholder screens untuk setiap menu
- Navigation logic

## Navigation Flow

```
Auth Success
    ↓
HomeScreen (Main Menu)
    ↓ (Click Menu)
    ├── Products Screen
    ├── History Screen
    ├── Expenses Screen
    ├── Reports Screen
    ├── Print Receipt Screen
    ├── Cashier Screen
    └── Settings Screen
```

## Implementation Details

### Grid Layout
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
)
```

### Menu Card Design
- **Size**: Fills width with 1:1 aspect ratio
- **Elevation**: 2dp (default), 8dp (pressed)
- **Icon Size**: 48dp
- **Spacing**: 12dp between icon and text
- **Text**: Title (titleMedium) + Description (bodySmall)

### Logout Flow
1. User clicks logout icon di TopAppBar
2. AlertDialog muncul untuk konfirmasi
3. Jika "Ya": navigate ke AUTH_GRAPH_ROUTE dan clear back stack
4. Jika "Batal": dismiss dialog

## Best Practices Applied

### 1. **Clean Architecture**
- ✅ Separation of concerns (UI, Domain, Navigation)
- ✅ Reusable components (MenuCard)
- ✅ Domain models (MenuItem)

### 2. **Material Design 3**
- ✅ Material 3 components
- ✅ Color scheme dan typography
- ✅ Elevation dan interaction states
- ✅ Responsive layout

### 3. **Jetpack Compose**
- ✅ Declarative UI
- ✅ State management
- ✅ Composition over inheritance
- ✅ Preview support

### 4. **Navigation**
- ✅ Type-safe navigation dengan routes
- ✅ Proper back stack management
- ✅ Modular navigation graph

### 5. **UX**
- ✅ Clear visual hierarchy
- ✅ Consistent spacing
- ✅ Touch target size (min 48dp)
- ✅ Confirmation dialog untuk destructive action (logout)
- ✅ Loading states dan error handling ready

## Placeholder Screens

Setiap menu item memiliki placeholder screen dengan:
- TopAppBar dengan back button
- Centered text: "Halaman [Menu Name] (Belum diimplementasi)"
- Proper navigation back

## Future Enhancements

1. **User Info Display**
   - Tampilkan nama user dan role di header
   - Avatar atau profile picture

2. **Quick Stats**
   - Ringkasan penjualan hari ini
   - Notifikasi atau badge untuk update

3. **Search & Filter**
   - Search menu items
   - Filter by category

4. **Customization**
   - User dapat customize menu order
   - Show/hide menu items based on role

5. **Animation**
   - Entry animation untuk cards
   - Transition animation antar screen

## Testing

### Manual Testing
1. Login dengan user valid
2. Verify HomeScreen muncul dengan 7 menu cards
3. Click setiap menu → verify navigasi ke placeholder screen
4. Click back button → verify kembali ke HomeScreen
5. Click logout → verify dialog muncul
6. Click "Ya" → verify navigate ke login screen

### Preview
HomeScreen memiliki `@Preview` annotation untuk testing di Android Studio Preview.

## Notes

- Menu icons menggunakan Material Icons Extended
- Grid layout responsive untuk berbagai ukuran layar
- Placeholder screens siap untuk implementasi fitur sebenarnya
- Logout functionality terintegrasi dengan auth system

