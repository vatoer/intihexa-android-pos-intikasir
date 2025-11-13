# Product Management Feature

## 📦 Overview
Complete CRUD implementation untuk manajemen produk dan kategori dengan clean architecture.

## ✨ Features

### Product Management
- ✅ **Daftar Produk** - Lihat semua produk dengan pagination
- ✅ **Tambah Produk** - Form lengkap dengan validasi
- ✅ **Edit Produk** - Update informasi produk
- ✅ **Hapus Produk** - Soft delete produk
- ✅ **Pencarian** - Cari produk berdasarkan nama, SKU, barcode
- ✅ **Filter Kategori** - Filter produk berdasarkan kategori
- ✅ **Filter Stok Menipis** - Alert untuk produk dengan stok rendah
- ✅ **Real-time Update** - Otomatis refresh dengan Flow

### Category Management
- ✅ **Kelola Kategori** - CRUD lengkap untuk kategori
- ✅ **Icon & Color** - Kustomisasi tampilan kategori
- ✅ **Organisasi Produk** - Kelompokkan produk per kategori

## 🏗️ Architecture

```
feature.product/
├── domain/
│   ├── repository/
│   │   └── ProductRepository.kt (Interface)
│   └── usecase/
│       ├── ProductUseCases.kt (6 use cases)
│       └── CategoryUseCases.kt (4 use cases)
│
├── data/
│   ├── repository/
│   │   └── ProductRepositoryImpl.kt
│   └── mapper/
│       └── ProductMapper.kt
│
├── ui/
│   ├── list/
│   │   ├── ProductListScreen.kt
│   │   ├── ProductListViewModel.kt
│   │   └── ProductListUiState.kt
│   ├── form/
│   │   ├── ProductFormScreen.kt
│   │   ├── ProductFormViewModel.kt
│   │   └── ProductFormUiState.kt
│   └── category/
│       ├── CategoryManagementScreen.kt
│       ├── CategoryManagementViewModel.kt
│       └── CategoryManagementUiState.kt
│
├── navigation/
│   ├── ProductRoutes.kt
│   └── ProductNavGraph.kt
│
└── di/
    └── ProductModule.kt
```

## 🎯 Use Cases

### Product Use Cases
1. **GetAllProductsUseCase** - Ambil semua produk
2. **GetProductByIdUseCase** - Ambil produk by ID
3. **SearchProductsUseCase** - Cari produk
4. **SaveProductUseCase** - Simpan/update produk
5. **DeleteProductUseCase** - Hapus produk
6. **GetLowStockProductsUseCase** - Produk stok menipis

### Category Use Cases
1. **GetAllCategoriesUseCase** - Ambil semua kategori
2. **GetCategoryByIdUseCase** - Ambil kategori by ID
3. **SaveCategoryUseCase** - Simpan/update kategori
4. **DeleteCategoryUseCase** - Hapus kategori

## 🚀 Quick Start

### 1. Setup Navigation
```kotlin
NavHost(
    navController = navController,
    startDestination = PRODUCT_GRAPH_ROUTE
) {
    productNavGraph(
        navController = navController,
        onNavigateBack = { /* handle back */ }
    )
}
```

### 2. Navigate to Product List
```kotlin
navController.navigate(PRODUCT_GRAPH_ROUTE)
```

### 3. Use in Your App
```kotlin
// From home screen
Button(onClick = { 
    navController.navigate(PRODUCT_GRAPH_ROUTE) 
}) {
    Text("Kelola Produk")
}
```

## 📱 Screens

### 1. Product List Screen
**Features:**
- Search bar dengan real-time search
- Filter kategori (chips)
- Filter stok menipis
- Card view untuk setiap produk
- Info: nama, harga, stok, kategori, SKU
- Actions: edit, delete
- Empty state dengan CTA
- FAB untuk tambah produk

**States:**
- Loading
- Empty
- Error
- Success dengan data

### 2. Product Form Screen
**Fields:**
- Nama Produk * (required)
- SKU
- Barcode
- Kategori (dropdown)
- Harga Jual * (required)
- Harga Modal
- Stok * (required)
- Stok Minimum
- Deskripsi
- URL Gambar
- Status Aktif (switch)

**Validation:**
- Nama tidak boleh kosong
- Harga harus > 0
- Stok harus >= 0
- Auto-format untuk currency

### 3. Category Management Screen
**Features:**
- List kategori dengan icon & color
- Add/Edit/Delete kategori
- Dialog form dengan:
  - Nama kategori
  - Deskripsi
  - Pilihan icon (emoji)
  - Pilihan warna (predefined)
- Empty state
- Konfirmasi hapus

## 💾 Data Models

### Product
```kotlin
data class Product(
    val id: String,
    val name: String,
    val sku: String?,
    val barcode: String?,
    val categoryId: String?,
    val categoryName: String?,
    val description: String?,
    val price: Double,
    val cost: Double?,
    val stock: Int,
    val minStock: Int,
    val imageUrl: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

### Category
```kotlin
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val order: Int,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

## 🎨 UI Components

### Custom Components
- **ProductCard** - Menampilkan info produk lengkap
- **CategoryChip** - Filter chip untuk kategori
- **CategoryFormDialog** - Dialog form kategori
- **EmptyState** - State untuk data kosong
- **SearchBar** - Input pencarian

## 🔄 Data Flow

```
User Action
    ↓
UI Event
    ↓
ViewModel
    ↓
Use Case
    ↓
Repository
    ↓
DAO (Room)
    ↓
Flow back to UI (reactive)
```

## ⚡ Features Highlights

### Real-time Updates
- Menggunakan Flow untuk reactive updates
- Perubahan data langsung terlihat di UI
- Tidak perlu manual refresh

### Search & Filter
- Real-time search saat mengetik
- Filter by kategori
- Filter stok menipis
- Kombinasi filter

### Validation
- Client-side validation
- Error messages yang jelas
- Prevent invalid data entry

### User Experience
- Loading states
- Error handling
- Empty states dengan CTA
- Confirmation dialogs
- Success feedback

## 🧪 Testing

### Sample Data
Gunakan test data untuk development:

```kotlin
// Add sample products
val sampleProducts = listOf(
    Product(
        id = UUID.randomUUID().toString(),
        name = "Nasi Goreng",
        price = 25000.0,
        stock = 100,
        minStock = 10,
        categoryId = "cat-1",
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
)
```

## 📊 Database Schema

### Product Entity
- id (PK)
- name
- sku
- barcode
- categoryId (FK)
- description
- price
- cost
- stock
- minStock
- imageUrl
- isActive
- isDeleted
- createdAt
- updatedAt
- syncedAt

### Category Entity
- id (PK)
- name
- description
- color
- icon
- order
- isActive
- isDeleted
- createdAt
- updatedAt
- syncedAt

## 🔐 Business Rules

1. **Product Name** - Required, min 1 character
2. **Price** - Required, must be > 0
3. **Stock** - Required, must be >= 0
4. **Min Stock** - Default 5 if not set
5. **Category** - Optional, can be null
6. **Delete** - Soft delete (isDeleted = true)

## 🎯 Best Practices

### Clean Architecture
- Domain layer tidak depend on framework
- Repository pattern untuk abstraksi
- Use case untuk business logic
- ViewModel untuk UI state

### State Management
- Single source of truth (StateFlow)
- Immutable UI state
- Event-based actions
- Reactive updates

### Error Handling
- Try-catch di repository
- User-friendly error messages
- Proper error states
- Snackbar untuk feedback

## 🚧 Future Enhancements

- [ ] Bulk operations
- [ ] Import/Export CSV
- [ ] Image upload
- [ ] Barcode scanner
- [ ] Product variants
- [ ] Stock history
- [ ] Price history
- [ ] Advanced filters
- [ ] Sort options
- [ ] Pagination
- [ ] Offline support
- [ ] Sync with backend

## 📚 Dependencies

- Hilt (DI)
- Room (Database)
- Jetpack Compose (UI)
- Navigation Compose
- Kotlin Coroutines & Flow
- Material 3

## ✅ Checklist Integration

- [x] Domain models created
- [x] Repository interface defined
- [x] Use cases implemented
- [x] Repository implementation
- [x] Mappers created
- [x] ViewModels implemented
- [x] UI screens designed
- [x] Navigation setup
- [x] DI module configured
- [x] Documentation written

## 🎊 Completed!

Fitur CRUD produk dan kategori sudah lengkap dan siap digunakan!

**Total Files Created:** 16 files
- Domain: 3 files
- Data: 2 files
- UI: 9 files
- Navigation: 2 files
- DI: 1 file

---

Built with ❤️ for IntiKasir POS

