# Complete Use Cases List - Product Feature

## ✅ All Use Cases Created

### Product Use Cases (7):

1. **GetAllProductsUseCase.kt** ✅
   - Get all products from repository
   - Returns: `Flow<List<Product>>`

2. **GetProductByIdUseCase.kt** ✅
   - Get single product by ID
   - Returns: `Flow<Product?>`

3. **SearchProductsUseCase.kt** ✅
   - Search products by query
   - Returns: `Flow<List<Product>>`

4. **GetProductsUseCase.kt** ✅ (Already existed)
   - Get products with filter & sort
   - Returns: `Flow<List<Product>>`

5. **GetLowStockProductsUseCase.kt** ✅
   - Get products with low stock
   - Returns: `Flow<List<Product>>`

6. **SaveProductUseCase.kt** ✅
   - Insert or update product
   - Returns: `suspend fun`

7. **DeleteProductUseCase.kt** ✅
   - Delete product by ID
   - Returns: `suspend fun`

---

### Category Use Cases (3):

1. **GetAllCategoriesUseCase.kt** ✅
   - Get all categories
   - Returns: `Flow<List<Category>>`

2. **SaveCategoryUseCase.kt** ✅
   - Insert or update category
   - Returns: `suspend fun`

3. **DeleteCategoryUseCase.kt** ✅
   - Delete category by ID
   - Returns: `suspend fun`

---

### Deprecated Files (2):

1. **ProductUseCases.kt** ⚠️ Deprecated
   - Contains deprecation notice only
   - Use individual use case files instead

2. **CategoryUseCases.kt** ⚠️ Deprecated
   - Contains deprecation notice only
   - Use individual use case files instead

---

## 📊 Summary:

**Total Use Cases:** 10 active files
**Product Use Cases:** 7 files
**Category Use Cases:** 3 files
**Deprecated Files:** 2 files (backward compatible)

**All use cases follow best practices:**
- ✅ Single Responsibility Principle
- ✅ Dependency Injection with Hilt
- ✅ Clean Architecture pattern
- ✅ Kotlin coroutines & Flow
- ✅ Proper documentation

---

## 🎯 Usage Examples:

### In ViewModel:
```kotlin
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {
    
    fun loadProducts() {
        viewModelScope.launch {
            getAllProductsUseCase().collect { products ->
                // Handle products
            }
        }
    }
}
```

### In CategoryManagementViewModel:
```kotlin
@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {
    
    fun saveCategory(category: Category) {
        viewModelScope.launch {
            saveCategoryUseCase(category)
        }
    }
}
```

---

## ✅ Status:

**All use cases are:**
- ✅ Created
- ✅ Properly injected
- ✅ Error-free
- ✅ Ready to use

**Project is ready to build!** 🚀

