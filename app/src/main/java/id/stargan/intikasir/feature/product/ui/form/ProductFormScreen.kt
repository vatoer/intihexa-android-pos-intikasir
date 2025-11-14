package id.stargan.intikasir.feature.product.ui.form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yalantis.ucrop.UCrop
import android.net.Uri
import java.io.File

// Simple visual transformation for thousand separator display (already formatted string)
private object NoOpTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(text, OffsetMapping.Identity)
    }
}

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
    val context = LocalContext.current

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val output = result.data?.let { UCrop.getOutput(it) }
            // Fallback: if output is null, we can’t do much; UCrop should return it properly. No-op otherwise.
            output?.let { viewModel.onEvent(ProductFormUiEvent.ImageCropped(it)) }
        }
    }

    fun launchCrop(input: Uri) {
        val dest = Uri.fromFile(File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            // Quality & behavior
            setCompressionQuality(85)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false)

            // Colors - toolbar (these override theme for UCrop internals)
            setToolbarColor(context.getColor(android.R.color.black))
            setStatusBarColor(context.getColor(android.R.color.black))
            setToolbarWidgetColor(context.getColor(android.R.color.white))

            // Colors - crop frame
            setActiveControlsWidgetColor(context.getColor(android.R.color.holo_green_dark))
            setRootViewBackgroundColor(context.getColor(android.R.color.black))
            setDimmedLayerColor(context.getColor(android.R.color.black))

            // Toolbar text
            setToolbarTitle("Crop Gambar")
            setToolbarCancelDrawable(android.R.drawable.ic_menu_close_clear_cancel)
            setToolbarCropDrawable(android.R.drawable.ic_menu_save)

            // Crop frame display
            setShowCropFrame(true)
            setShowCropGrid(true)
            setCropGridStrokeWidth(2)
            setCropGridColor(context.getColor(android.R.color.white))
            setCropFrameColor(context.getColor(android.R.color.white))

            // Image settings
            setCircleDimmedLayer(false)
            setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)

            // No rounded corners for cropped image
            withMaxResultSize(1080, 1080)
        }
        val intent = UCrop.of(input, dest)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .withOptions(options)
            .getIntent(context)
        cropLauncher.launch(intent)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { launchCrop(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { launchCrop(it) }
        }
    }

    val onPickImage: () -> Unit = { galleryLauncher.launch("image/*") }
    val onCaptureImage: () -> Unit = {
        val file = File(context.cacheDir, "cap_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

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
                contentAlignment = Alignment.Center
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

                // Barcode with scan icon
                OutlinedTextField(
                    value = uiState.barcode,
                    onValueChange = { viewModel.onEvent(ProductFormUiEvent.BarcodeChanged(it)) },
                    label = { Text("Barcode") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.onEvent(ProductFormUiEvent.ScanBarcode) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                        }
                    }
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

                // Price with thousand separator
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { input ->
                        val raw = input.filter { it.isDigit() }
                        val formatted = raw.chunked(3).let { // naive grouping from end
                            if (raw.isEmpty()) "" else raw.reversed().chunked(3).joinToString(".") { it }.reversed()
                        }
                        viewModel.onEvent(ProductFormUiEvent.PriceChanged(formatted, raw))
                    },
                    label = { Text("Harga Jual *") },
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    visualTransformation = NoOpTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Cost
                OutlinedTextField(
                    value = uiState.cost,
                    onValueChange = { input ->
                        val raw = input.filter { it.isDigit() }
                        val formatted = if (raw.isEmpty()) "" else raw.reversed().chunked(3).joinToString(".") { it }.reversed()
                        viewModel.onEvent(ProductFormUiEvent.CostChanged(formatted, raw))
                    },
                    label = { Text("Harga Modal") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    visualTransformation = NoOpTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

                // Image Picker Section
                Text("Gambar Produk", style = MaterialTheme.typography.titleSmall)
                val preview = uiState.imagePreviewUri
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        if (preview != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(preview)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preview Gambar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(onClick = { viewModel.onEvent(ProductFormUiEvent.RemoveImage) }, label = { Text("Hapus") })
                                AssistChip(onClick = { viewModel.onEvent(ProductFormUiEvent.PickImage) }, label = { Text("Ganti") })
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { viewModel.onEvent(ProductFormUiEvent.PickImage) },
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Pilih / Ambil Gambar", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onPickImage) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Galeri")
                    }
                    OutlinedButton(onClick = onCaptureImage) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Kamera")
                    }
                }

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
