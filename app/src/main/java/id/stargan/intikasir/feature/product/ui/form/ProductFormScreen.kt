package id.stargan.intikasir.feature.product.ui.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Product Form Screen - Add/Edit Product
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle navigation back on success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(ProductFormUiEvent.DismissError)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Produk" else "Tambah Produk")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding() // Add this to handle keyboard padding
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Name
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.NameChanged(it)) },
                    label = { Text("Nama Produk *") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // SKU
                OutlinedTextField(
                    value = uiState.sku,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.SkuChanged(it)) },
                    label = { Text("SKU") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Barcode
                OutlinedTextField(
                    value = uiState.barcode,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.BarcodeChanged(it)) },
                    label = { Text("Barcode") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Dropdown
                var expandedCategory by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = uiState.categories.find { it.id == uiState.categoryId }?.name ?: "Pilih Kategori",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tanpa Kategori") },
                            onClick = {
                                viewModel.onEvent(ProductFormUiEvent.CategoryChanged(""))
                                expandedCategory = false
                            }
                        )
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.onEvent(ProductFormUiEvent.CategoryChanged(category.id))
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Price
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.PriceChanged(it)) },
                    label = { Text("Harga Jual *") },
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("Rp ") },
                    singleLine = true
                )

                // Cost
                OutlinedTextField(
                    value = uiState.cost,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.CostChanged(it)) },
                    label = { Text("Harga Modal") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("Rp ") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stock
                    OutlinedTextField(
                        value = uiState.stock,
                        onValueChange = { viewModel.onEvent(ProductFormUiEvent.StockChanged(it)) },
                        label = { Text("Stok *") },
                        isError = uiState.stockError != null,
                        supportingText = uiState.stockError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Min Stock
                    OutlinedTextField(
                        value = uiState.minStock,
                        onValueChange = { viewModel.onEvent(ProductFormUiEvent.MinStockChanged(it)) },
                        label = { Text("Stok Minimum") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.DescriptionChanged(it)) },
                    label = { Text("Deskripsi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                // Image URL
                OutlinedTextField(
                    value = uiState.imageUrl,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.ImageUrlChanged(it)) },
                    label = { Text("URL Gambar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Active Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Produk Aktif")
                    Switch(
                        checked = uiState.isActive,
                        onCheckedChange = {
                            viewModel.onEvent(ProductFormUiEvent.ActiveChanged(it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = { viewModel.onEvent(ProductFormUiEvent.SaveProduct) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isSaving) "Menyimpan..." else "Simpan")
                }
            }
        }
    }
}

