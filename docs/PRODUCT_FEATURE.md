## Product Feature - Complete Implementation

## Overview
Fitur Product Management dengan kemampuan:
- List produk dengan filter dan sort
- Search produk
- Detail produk
- CRUD produk (Admin only)
- Kategori management (Admin only)
- Role-based access control

## Features

### 1. **Product List Screen**
- ✅ Menampilkan daftar produk dalam list
- ✅ Product card dengan image di kiri, info di kanan
- ✅ Filter berdasarkan kategori, stok, status
- ✅ Sort berdasarkan nama, harga, stok, tanggal
- ✅ Search produk by nama/deskripsi
- ✅ Stock badge (Out of stock, Low stock, Normal)
- ✅ Category chip pada setiap item
- ✅ Role-based UI (Admin vs Non-Admin)

### 2. **Filter Options**
- Kategori (Semua / Per kategori)
- Hanya yang tersedia (In stock only)
- Stok menipis (Low stock only)
- Hanya produk aktif

### 3. **Sort Options**
- Nama A-Z / Z-A
- Harga Termurah / Termahal
- Stok Terendah / Tertinggi
- Terbaru / Terlama

### 4. **Admin Features**
- Tombol FAB untuk tambah produk baru
- Icon button "Kelola Kategori" di toolbar
- Akses ke edit/delete produk

### 5. **Category Management Flow**
**Jawaban untuk pertanyaan: "untuk kategori, dimanakan sebaiknya flow masuk untuk pengaturan/manajemen kategori?"**

✅ **Recommended Flow (Implemented):**
```
Product List Screen (Admin)
    ↓ (Click Category Icon di TopBar)
Category Management Screen
    ├── List semua kategori
    ├── Tambah kategori baru
    ├── Edit kategori
    └── Hapus kategori
```

**Mengapa di Product List Screen?**
1. ✅ **Context-aware**: User sedang di product context
2. ✅ **Easy access**: Admin bisa langsung manage saat melihat products
3. ✅ **Workflow**: Create category → Create product (seamless)
4. ✅ **Admin-only**: Icon hanya muncul untuk admin

**Alternative locations (Not implemented):**
- ❌ Settings: Terlalu jauh dari product context
- ❌ Home Menu: Bisa, tapi kurang contextual
- ❌ Sidebar: App tidak menggunakan sidebar pattern

## Architecture

### Clean Architecture Layers

```
presentation/
├── ui/
│   ├── list/
│   │   ├── ProductListScreen.kt
│   │   ├── ProductListViewModel.kt
│   │   └── ProductListUiState.kt
│   └── components/
│       ├── ProductListItem.kt
│       ├── ProductFilterDialog.kt
│       └── ProductSortDialog.kt
└── navigation/
    └── ProductRoutes.kt

domain/
├── model/
│   └── ProductFilter.kt
├── usecase/
│   ├── GetProductsUseCase.kt
│   └── GetCategoriesUseCase.kt
└── repository/
    └── ProductRepository.kt

data/
└── repository/
    └── ProductRepositoryImpl.kt (To be implemented)
```

## UI Components

### 1. **ProductListItem**
```kotlin
Card {
    Row {
        [Image 80x80dp]  [Name]
                         [Description]
                         [Price]
                         [Stock Badge + Category Chip]
    }
}
```

**Features:**
- Image di kiri (80x80dp)
- Placeholder icon jika no image
- Info lengkap di kanan
- Stock badge dengan warna:
  - Red: Out of stock
  - Orange: Low stock
  - Grey: Normal
- Category chip (optional)

### 2. **ProductFilterDialog**
- Radio buttons untuk kategori
- Checkboxes untuk filter stok
- Checkbox untuk produk aktif
- Terapkan / Batal buttons

### 3. **ProductSortDialog**
- Radio buttons untuk sort options
- Grouping dengan divider
- Terapkan / Batal buttons

### 4. **ProductListScreen**
```
TopAppBar
├── Back button
├── Title / Search field
└── Actions:
    ├── Search icon (toggle)
    ├── Sort icon
    ├── Filter icon
    └── Category icon (Admin only)

Body:
├── Loading indicator
├── Error content
├── Empty content
└── LazyColumn (Product list)

FAB (Admin only):
└── Add Product button
```

## Navigation Flow

```
Home Screen
    ↓ (Click "Produk")
Product List Screen
    ├── Click Product
    │   ↓
    │   Product Detail Screen
    │       └── (Admin) Edit Product
    │
    ├── (Admin) Click FAB
    │   ↓
    │   Add Product Screen
    │
    └── (Admin) Click Category Icon
        ↓
        Category Management Screen
```

## State Management

### ProductListUiState
```kotlin
data class ProductListUiState(
    val products: List<Product>,
    val categories: List<Category>,
    val isLoading: Boolean,
    val error: String?,
    val searchQuery: String,
    val currentFilter: ProductFilter,
    val currentSort: ProductSortBy,
    val showFilterDialog: Boolean,
    val showSortDialog: Boolean,
    val isAdmin: Boolean
)
```

### Events
```kotlin
sealed class ProductListUiEvent {
    SearchQueryChanged
    FilterChanged
    SortChanged
    ProductClicked
    AddProductClicked
    ManageCategoriesClicked
    ShowFilterDialog / HideFilterDialog
    ShowSortDialog / HideSortDialog
    ClearFilter
    Refresh
}
```

## Data Flow

```
User Action (UI Event)
    ↓
ViewModel.onEvent()
    ↓
Use Case (Business Logic)
    ↓
Repository (Data Source)
    ↓
Database / Network
    ↓ (Flow)
UI State Update
    ↓
UI Re-composition
```

## Filter & Sort Logic

### Filtering Process (GetProductsUseCase):
1. Get all products from repository
2. Apply category filter
3. Apply price range filter
4. Apply stock filters (in stock / low stock)
5. Apply active filter
6. Apply sorting
7. Return filtered & sorted list

### Search Logic (ViewModel):
- Search dilakukan di ViewModel level
- Filter by name or description (case-insensitive)
- Combined dengan filter & sort dari use case

## Role-Based Features

### Admin
✅ Can see:
- FAB untuk tambah produk
- Category management icon
- Edit/delete options di detail

✅ Can do:
- Add new product
- Edit product
- Delete product
- Manage categories

### Non-Admin (Cashier)
✅ Can see:
- Product list
- Product detail (read-only)
- Search, filter, sort

❌ Cannot:
- Add/edit/delete product
- Manage categories

## UI/UX Best Practices

### 1. **Search Experience**
- Toggle search dengan icon
- TextField di TopBar saat active
- Close button untuk clear & exit
- Real-time search

### 2. **Filter & Sort**
- Dialog modal (tidak mengganggu)
- Preview current selection
- Terapkan / Batal options
- Visual feedback (radio/checkbox)

### 3. **Empty States**
- Icon + message
- Call to action (Add product)
- Friendly messaging

### 4. **Error Handling**
- Error icon + message
- Retry button
- Clear error explanation

### 5. **Loading States**
- Centered CircularProgressIndicator
- Non-blocking (tidak freeze UI)

### 6. **Stock Indicators**
- Color-coded badges
- Warning icon untuk low/out of stock
- Clear visual hierarchy

## Implementation Status

### ✅ Completed:
1. Domain layer (Repository, Use Cases, Models)
2. Presentation layer (ViewModel, UI State, Events)
3. UI Components (Screen, Item, Dialogs)
4. Navigation (Routes, Integration)
5. Role-based access control
6. Filter & Sort logic
7. Search functionality
8. Category management entry point

### 🔄 To be Implemented:
1. Data layer (RepositoryImpl, DAO, Entity)
2. Product Detail Screen
3. Product Add/Edit Screen
4. Category Management Screen
5. Image upload functionality
6. Barcode scanner integration

## Testing Scenarios

### Test Case 1: View Products (Non-Admin)
1. Login sebagai Cashier
2. Navigate ke Product List
3. ✅ See product list
4. ✅ No FAB visible
5. ✅ No Category management icon
6. Click product → see detail (read-only)

### Test Case 2: Manage Products (Admin)
1. Login sebagai Admin
2. Navigate ke Product List
3. ✅ See product list
4. ✅ FAB visible
5. ✅ Category management icon visible
6. Click FAB → Add Product Screen
7. Click Category icon → Category Management

### Test Case 3: Filter Products
1. Open Product List
2. Click Filter icon
3. Select kategori "Makanan"
4. Check "Hanya yang tersedia"
5. Click "Terapkan"
6. ✅ See filtered products

### Test Case 4: Sort Products
1. Open Product List
2. Click Sort icon
3. Select "Harga Termurah"
4. Click "Terapkan"
5. ✅ Products sorted by price ascending

### Test Case 5: Search Products
1. Click Search icon
2. Type "Kopi"
3. ✅ See filtered products containing "Kopi"
4. Click Close
5. ✅ Clear search, show all products

## Performance Considerations

### 1. **LazyColumn**
- Items rendered on demand
- Key untuk efficient recomposition
- Proper item spacing

### 2. **Image Loading**
- Coil untuk async loading
- Placeholder during load
- Memory caching

### 3. **Flow Collection**
- StateFlow untuk UI state
- Collect in viewModelScope
- Automatic cleanup

### 4. **Search Optimization**
- Debounce tidak diperlukan (simple filter)
- Case-insensitive search
- Filter di memory (fast)

## Best Practices Applied

1. ✅ **Clean Architecture**: Clear layer separation
2. ✅ **SOLID Principles**: Single responsibility
3. ✅ **Material Design 3**: Modern UI components
4. ✅ **State Management**: Unidirectional data flow
5. ✅ **Role-Based Access**: Security & UX
6. ✅ **Error Handling**: Graceful failures
7. ✅ **Empty States**: User guidance
8. ✅ **Loading States**: User feedback
9. ✅ **Responsive UI**: Adaptive layouts
10. ✅ **Testable Code**: Each layer can be tested

## Future Enhancements

1. **Bulk Operations**
   - Select multiple products
   - Bulk edit/delete
   - Export to CSV/Excel

2. **Advanced Filters**
   - Price range slider
   - Date range picker
   - Multiple category selection

3. **Product Analytics**
   - Most viewed products
   - Low stock alerts
   - Sales performance

4. **Barcode Integration**
   - Scan barcode untuk search
   - Generate barcode untuk produk
   - Barcode printer support

5. **Image Management**
   - Multiple images per product
   - Image gallery
   - Compress & optimize

6. **Category Hierarchy**
   - Sub-categories
   - Category tree view
   - Nested filtering

## Summary

Fitur Product telah diimplementasikan dengan:
- ✅ **Complete list functionality**
- ✅ **Filter & Sort options**
- ✅ **Search capability**
- ✅ **Role-based access control**
- ✅ **Category management entry point di Product List**
- ✅ **Clean architecture**
- ✅ **Material 3 design**
- ✅ **Best practices UI/UX**

**Entry point untuk Category Management:**
Admin dapat mengakses Category Management melalui icon di TopBar Product List Screen (icon Category). Ini adalah lokasi yang paling contextual dan sesuai dengan workflow natural user.

Placeholder screens sudah ready untuk implementasi selanjutnya (Detail, Add/Edit, Category Management).

