package id.stargan.intikasir.feature.product.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.product.ui.components.ProductFilterDialog
import id.stargan.intikasir.feature.product.ui.components.ProductListItem
import id.stargan.intikasir.feature.product.ui.components.ProductSortDialog
import id.stargan.intikasir.feature.security.ui.SecuritySettingsViewModel
import id.stargan.intikasir.feature.security.util.usePermission

/**
 * Product List Screen
 * Menampilkan daftar produk dengan filter, sort, dan search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onProductClick: (String) -> Unit,
    onAddProductClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Example enforcement: show FAB if admin or cashier has create product permission
    val securityVm: SecuritySettingsViewModel = hiltViewModel()
    val canCreateProduct = usePermission(securityVm.observePermission("CASHIER") { it.canCreateProduct })

    Scaffold(
        topBar = {
            ProductListTopBar(
                onFilterClick = {
                    viewModel.onEvent(ProductListUiEvent.ShowFilterDialog)
                },
                onSortClick = {
                    viewModel.onEvent(ProductListUiEvent.ShowSortDialog)
                },
                onManageCategoriesClick = {
                    viewModel.onEvent(ProductListUiEvent.ManageCategoriesClicked)
                    onManageCategoriesClick()
                },
                onBackClick = onBackClick,
                isAdmin = uiState.isAdmin
            )
        },
        floatingActionButton = {
            if (uiState.isAdmin || canCreateProduct) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(ProductListUiEvent.AddProductClicked)
                        onAddProductClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Produk"
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = {
                    viewModel.onEvent(ProductListUiEvent.SearchQueryChanged(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Active Filter Chips
            ActiveFilterChips(
                uiState = uiState,
                onClearFilter = { viewModel.onEvent(ProductListUiEvent.FilterChanged(id.stargan.intikasir.feature.product.domain.model.ProductFilter())) },
                onClearCategoryFilter = {
                    viewModel.onEvent(ProductListUiEvent.FilterChanged(uiState.currentFilter.copy(categoryId = null)))
                },
                onClearPriceFilter = {
                    viewModel.onEvent(ProductListUiEvent.FilterChanged(uiState.currentFilter.copy(minPrice = null, maxPrice = null)))
                }
            )

            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        ErrorContent(
                            error = uiState.error!!,
                            onRetry = { viewModel.onEvent(ProductListUiEvent.RefreshProducts) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.products.isEmpty() -> {
                        EmptyContent(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.products,
                                key = { it.id }
                            ) { product ->
                                ProductListItem(
                                    product = product,
                                    onClick = {
                                        viewModel.onEvent(ProductListUiEvent.ProductClicked(product.id))
                                        onProductClick(product.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Dialog
        if (uiState.showFilterDialog) {
            ProductFilterDialog(
                currentFilter = uiState.currentFilter,
                categories = uiState.categories,
                onFilterChanged = {
                    viewModel.onEvent(ProductListUiEvent.FilterChanged(it))
                },
                onDismiss = {
                    viewModel.onEvent(ProductListUiEvent.HideFilterDialog)
                }
            )
        }

        // Sort Dialog
        if (uiState.showSortDialog) {
            ProductSortDialog(
                currentSort = uiState.currentSort,
                onSortChanged = {
                    viewModel.onEvent(ProductListUiEvent.SortChanged(it))
                },
                onDismiss = {
                    viewModel.onEvent(ProductListUiEvent.HideSortDialog)
                }
            )
        }
    }
}

/**
 * Top Bar dengan actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductListTopBar(
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text("Daftar Produk") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        },
        actions = {
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Urutkan"
                )
            }

            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Filter"
                )
            }

            if (isAdmin) {
                IconButton(onClick = onManageCategoriesClick) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Kelola Kategori"
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Search Bar Component
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Cari produk...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * Active Filter Chips - Shows currently active filters
 */
@Composable
private fun ActiveFilterChips(
    uiState: ProductListUiState,
    onClearFilter: () -> Unit,
    onClearCategoryFilter: () -> Unit,
    onClearPriceFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filter = uiState.currentFilter
    val hasActiveFilters = filter.categoryId != null ||
                          filter.inStockOnly ||
                          filter.lowStockOnly ||
                          !filter.activeOnly ||
                          filter.minPrice != null ||
                          filter.maxPrice != null

    if (hasActiveFilters) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category chip
            filter.categoryId?.let { catId ->
                val categoryName = uiState.categories.find { it.id == catId }?.name ?: "Kategori"
                item {
                    FilterChip(
                        selected = true,
                        onClick = onClearCategoryFilter,
                        label = { Text(categoryName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // In Stock chip
            if (filter.inStockOnly) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { /* handled by clear all */ },
                        label = { Text("Tersedia") }
                    )
                }
            }

            // Low Stock chip
            if (filter.lowStockOnly) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { /* handled by clear all */ },
                        label = { Text("Stok Menipis") }
                    )
                }
            }

            // Inactive products chip
            if (!filter.activeOnly) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { /* handled by clear all */ },
                        label = { Text("Termasuk Nonaktif") }
                    )
                }
            }

            // Price range chip
            if (filter.minPrice != null || filter.maxPrice != null) {
                item {
                    val priceText = buildString {
                        append("Rp ")
                        if (filter.minPrice != null && filter.maxPrice != null) {
                            append("${filter.minPrice.toInt()}-${filter.maxPrice.toInt()}")
                        } else if (filter.minPrice != null) {
                            append("≥ ${filter.minPrice.toInt()}")
                        } else {
                            append("≤ ${filter.maxPrice!!.toInt()}")
                        }
                    }
                    FilterChip(
                        selected = true,
                        onClick = onClearPriceFilter,
                        label = { Text(priceText) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus filter harga",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Clear all filters
            item {
                AssistChip(
                    onClick = onClearFilter,
                    label = { Text("Hapus Semua") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Empty Content
 */
@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tidak ada produk",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tambahkan produk baru untuk memulai",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error Content
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Terjadi Kesalahan",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry) {
            Text("Coba Lagi")
        }
    }
}
