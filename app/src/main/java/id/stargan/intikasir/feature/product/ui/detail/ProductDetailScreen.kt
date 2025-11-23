package id.stargan.intikasir.feature.product.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import id.stargan.intikasir.feature.security.ui.SecuritySettingsViewModel
import id.stargan.intikasir.feature.security.util.usePermission
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.home.navigation.HistoryRoleViewModel
import id.stargan.intikasir.domain.model.UserRole

/**
 * Product Detail Screen
 * Menampilkan detail lengkap produk
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onEditProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back when delete success
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    // Determine permissions
    val securityVm: SecuritySettingsViewModel = hiltViewModel()
    val canEditProduct = usePermission(securityVm.observePermission("CASHIER") { it.canEditProduct })
    val canDeleteProduct = usePermission(securityVm.observePermission("CASHIER") { it.canDeleteProduct })
    val getCurrentUserUseCase: GetCurrentUserUseCase = hiltViewModel<HistoryRoleViewModel>().getCurrentUserUseCase
    val currentUser by getCurrentUserUseCase().collectAsState(initial = null)
    val isAdmin = currentUser?.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (uiState.product != null && (isAdmin || canEditProduct)) {
                        IconButton(
                            onClick = {
                                uiState.product?.let { onEditProduct(it.id) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    }

                    if (uiState.product != null && (isAdmin || canDeleteProduct)) {
                        IconButton(
                            onClick = { viewModel.onEvent(ProductDetailUiEvent.DeleteProduct) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
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

                uiState.error != null && uiState.product == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Produk tidak ditemukan",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Kembali")
                        }
                    }
                }

                uiState.product != null -> {
                    ProductDetailContent(
                        product = uiState.product!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Loading overlay saat deleting
            if (uiState.isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.onEvent(ProductDetailUiEvent.CancelDelete)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Hapus Produk?") },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menghapus produk \"${uiState.product?.name}\"? " +
                        "Tindakan ini tidak dapat dibatalkan."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onEvent(ProductDetailUiEvent.ConfirmDelete)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(ProductDetailUiEvent.CancelDelete)
                        }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }

        // Error Snackbar
        uiState.error?.let { error ->
            if (uiState.product != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(ProductDetailUiEvent.DismissError)
                            }
                        ) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: id.stargan.intikasir.domain.model.Product,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Product Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Product Name
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category
        if (product.categoryName != null) {
            SuggestionChip(
                onClick = { },
                label = { Text(product.categoryName) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Price Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Harga Jual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = product.formattedPrice,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (product.cost != null && product.cost > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Harga Modal: ${product.formattedCost}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stock Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stok",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Stock Badge
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${product.stock} unit",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when {
                                    product.isOutOfStock -> Icons.Default.Cancel
                                    product.isLowStock -> Icons.Default.Warning
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when {
                                product.isOutOfStock -> MaterialTheme.colorScheme.errorContainer
                                product.isLowStock -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    )
                }

                if (product.lowStockThreshold != null && product.lowStockThreshold > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Batas Stok Minimum: ${product.lowStockThreshold} unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Details
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Informasi Produk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (!product.description.isNullOrBlank()) {
                    DetailRow(
                        label = "Deskripsi",
                        value = product.description
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // SKU
                if (!product.sku.isNullOrBlank()) {
                    DetailRow(
                        label = "SKU",
                        value = product.sku
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Barcode
                if (!product.barcode.isNullOrBlank()) {
                    DetailRow(
                        label = "Barcode",
                        value = product.barcode
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Status
                DetailRow(
                    label = "Status",
                    value = if (product.isActive) "Aktif" else "Tidak Aktif"
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
