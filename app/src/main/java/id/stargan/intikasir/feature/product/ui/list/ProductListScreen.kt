package id.stargan.intikasir.feature.product.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.stargan.intikasir.feature.product.ui.components.ProductFilterDialog
import id.stargan.intikasir.feature.product.ui.components.ProductListItem
import id.stargan.intikasir.feature.product.ui.components.ProductSortDialog
import id.stargan.intikasir.feature.product.ui.list.ProductListUiState
import id.stargan.intikasir.feature.product.ui.list.ProductListUiEvent
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

    Scaffold(
        topBar = {
            ProductListTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = {
                    viewModel.onEvent(ProductListUiEvent.SearchQueryChanged(it))
                },
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
            if (uiState.isAdmin) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        contentPadding = PaddingValues(16.dp),
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
 * Top Bar dengan search dan actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductListTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Cari produk...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    Text("Daftar Produk")
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            },
            actions = {
                if (!isSearchActive) {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari"
                        )
                    }
                } else {
                    IconButton(onClick = {
                        isSearchActive = false
                        onSearchQueryChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup"
                        )
                    }
                }

                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Urutkan"
                    )
                }

                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
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
            }
        )
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


