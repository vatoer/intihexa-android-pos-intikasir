package id.stargan.intikasir.feature.settings.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.pos.print.ReceiptPrinter
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.PaymentMethod
import java.io.File
import java.util.UUID
import kotlinx.coroutines.launch
import id.stargan.intikasir.feature.pos.print.ESCPosPrinter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val output = result.data?.let { UCrop.getOutput(it) }
            output?.let { viewModel.onEvent(StoreSettingsUiEvent.LogoCropped(it)) }
        }
    }

    fun launchCrop(input: Uri) {
        val dest = Uri.fromFile(File(context.cacheDir, "logo_crop_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            // Quality & behavior
            setCompressionQuality(85)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false)

            // Colors - toolbar
            setToolbarColor(context.getColor(android.R.color.black))
            setStatusBarColor(context.getColor(android.R.color.black))
            setToolbarWidgetColor(context.getColor(android.R.color.white))

            // Colors - crop frame
            setActiveControlsWidgetColor(context.getColor(android.R.color.holo_green_dark))
            setRootViewBackgroundColor(context.getColor(android.R.color.black))
            setDimmedLayerColor(context.getColor(android.R.color.black))

            // Toolbar text
            setToolbarTitle("Crop Logo Toko")
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
        }
        val intent = UCrop.of(input, dest)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .withOptions(options)
            .getIntent(context)
        cropLauncher.launch(intent)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { launchCrop(it) } }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { launchCrop(it) }
    }

    val onPickLogo = { galleryLauncher.launch("image/*") }
    val onCaptureLogo = {
        val file = File(context.cacheDir, "logo_cap_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(StoreSettingsUiEvent.DismissError)
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(StoreSettingsUiEvent.DismissSuccess)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Toko") },
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Logo Toko",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Logo Preview
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.logoPreviewUri != null) {
                                AsyncImage(
                                    model = uiState.logoPreviewUri,
                                    contentDescription = "Logo Toko",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    onClick = onPickLogo
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Store,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = "Tambah Logo",
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }

                            if (uiState.isImageProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.logoPreviewUri != null) {
                                OutlinedButton(
                                    onClick = { viewModel.onEvent(StoreSettingsUiEvent.RemoveLogo) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Hapus")
                                }
                            }

                            OutlinedButton(
                                onClick = onPickLogo,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (uiState.logoPreviewUri != null) "Ganti" else "Galeri")
                            }

                            OutlinedButton(
                                onClick = onCaptureLogo,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Kamera")
                            }
                        }

                        Text(
                            text = "Logo akan ditampilkan pada struk penjualan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Store Info Section (Optional - for future enhancement)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Informasi Toko",
                            style = MaterialTheme.typography.titleMedium
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        InfoRow(
                            label = "Nama Toko",
                            value = uiState.settings?.storeName ?: "Belum diatur"
                        )
                        InfoRow(
                            label = "Alamat",
                            value = uiState.settings?.storeAddress ?: "Belum diatur"
                        )
                        InfoRow(
                            label = "Telepon",
                            value = uiState.settings?.storePhone ?: "Belum diatur"
                        )
                    }
                }

                // Printing Settings Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val settings = uiState.settings ?: StoreSettings()
                    var paperWidth by remember(settings) { mutableStateOf(settings.paperWidthMm) }
                    var charPerLine by remember(settings) { mutableStateOf(settings.paperCharPerLine) }
                    val format = "THERMAL" // lock to thermal only
                    var autoCut by remember(settings) { mutableStateOf(settings.autoCut) }
                    var printLogo by remember(settings) { mutableStateOf(settings.printLogo) }
                    var useEscPosDirect by remember(settings) { mutableStateOf(settings.useEscPosDirect) }
                    val hasActivePrinter = !((uiState.settings?.printerAddress).isNullOrBlank())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Pengaturan Cetak", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()

                        // Format info (locked to Thermal)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Format Cetak")
                            AssistChip(onClick = {}, label = { Text("Thermal (aktif)") }, leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }, enabled = false)
                        }

                        // Paper width selector
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Lebar Kertas (mm)")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = paperWidth == 58,
                                    onClick = { paperWidth = 58; charPerLine = 32 },
                                    label = { Text("58") },
                                    leadingIcon = if (paperWidth == 58) { { Icon(Icons.Default.Check, contentDescription = null) } } else null
                                )
                                FilterChip(
                                    selected = paperWidth == 80,
                                    onClick = { paperWidth = 80; charPerLine = 48 },
                                    label = { Text("80") },
                                    leadingIcon = if (paperWidth == 80) { { Icon(Icons.Default.Check, contentDescription = null) } } else null
                                )
                            }
                        }
                        // Hint chars per line
                        Text(
                            text = if (paperWidth == 58) "58 mm ≈ 32 karakter per baris" else "80 mm ≈ 48 karakter per baris",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Options
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto Cut (jika didukung)")
                            Switch(checked = autoCut, onCheckedChange = { autoCut = it })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tampilkan Logo di Struk")
                            Switch(checked = printLogo, onCheckedChange = { printLogo = it })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Cetak langsung ESC/POS (Bluetooth)")
                            Switch(
                                checked = useEscPosDirect,
                                onCheckedChange = { useEscPosDirect = it },
                                enabled = hasActivePrinter
                            )
                        }
                        if (!hasActivePrinter) {
                            Text(
                                text = "Pilih printer terlebih dahulu untuk mengaktifkan cetak langsung.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Mini preview (generate thermal pdf and show hint)
                        Text("Preview Thermal (contoh)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "Preview PDF akan dibuat saat Cetak/Bagikan. Untuk melihat contoh, selesaikan transaksi lalu buka struk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                try {
                                    val effective = uiState.settings ?: StoreSettings()
                                    val txId = UUID.randomUUID().toString()
                                    val items = listOf(
                                        TransactionItemEntity(
                                            transactionId = txId,
                                            productId = "sku-001",
                                            productName = "Produk Contoh A",
                                            productPrice = 10000.0,
                                            quantity = 2,
                                            unitPrice = 10000.0,
                                            subtotal = 20000.0
                                        ),
                                        TransactionItemEntity(
                                            transactionId = txId,
                                            productId = "sku-002",
                                            productName = "Produk Contoh B",
                                            productPrice = 5000.0,
                                            quantity = 1,
                                            unitPrice = 5000.0,
                                            subtotal = 5000.0
                                        )
                                    )
                                    val subtotal = items.sumOf { it.subtotal }
                                    val tax = if (effective.taxEnabled) subtotal * (effective.taxPercentage / 100.0) else 0.0
                                    val total = subtotal + tax
                                    val tx = TransactionEntity(
                                        transactionNumber = "PREVIEW-${System.currentTimeMillis()}",
                                        cashierId = "preview",
                                        cashierName = "Preview",
                                        paymentMethod = PaymentMethod.CASH,
                                        subtotal = subtotal,
                                        tax = tax,
                                        total = total,
                                        cashReceived = total,
                                        cashChange = 0.0
                                    )
                                    val result = ReceiptPrinter.generateThermalReceiptPdf(context, effective, tx, items)
                                    val file = File(result.pdfUri.path ?: return@OutlinedButton)
                                    val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(contentUri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val pm = context.packageManager
                                    if (intent.resolveActivity(pm) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback: share chooser
                                        ReceiptPrinter.sharePdf(context, contentUri)
                                    }
                                } catch (e: Exception) {
                                    // Ignore preview errors for now
                                }
                            },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Preview, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Preview Struk (Thermal)")
                        }

                        // Save button
                        Button(
                            onClick = {
                                val current = uiState.settings ?: StoreSettings()
                                val updated = current.copy(
                                    paperWidthMm = paperWidth,
                                    paperCharPerLine = charPerLine,
                                    printFormat = format,
                                    autoCut = autoCut,
                                    printLogo = printLogo,
                                    useEscPosDirect = useEscPosDirect,
                                    updatedAt = System.currentTimeMillis()
                                )
                                viewModel.onEvent(StoreSettingsUiEvent.Save(updated))
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Simpan Pengaturan")
                        }
                    }
                }

                // Bluetooth Printer Picker Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val adapter = remember { BluetoothAdapter.getDefaultAdapter() }
                    val btPermission = Manifest.permission.BLUETOOTH_CONNECT
                    val sdk31Plus = remember { android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S }
                    var hasBtPermission by remember {
                        mutableStateOf(
                            if (sdk31Plus) ContextCompat.checkSelfPermission(context, btPermission) == PackageManager.PERMISSION_GRANTED else true
                        )
                    }
                    val requestBtPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        hasBtPermission = granted
                    }
                    val bonded = remember(hasBtPermission) { if (hasBtPermission) adapter?.bondedDevices?.toList().orEmpty() else emptyList() }
                    var selectedAddress by remember(uiState.settings) { mutableStateOf(uiState.settings?.printerAddress) }
                    var selectedName by remember(uiState.settings) { mutableStateOf(uiState.settings?.printerName) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Pilih Printer Bluetooth", style = MaterialTheme.typography.titleMedium)
                        val activeName = uiState.settings?.printerName
                        val activeAddr = uiState.settings?.printerAddress
                        if (!activeAddr.isNullOrBlank()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Printer aktif:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(activeName ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                                    Text(activeAddr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        // Test Print button
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = {
                                when {
                                    adapter == null -> scope.launch { snackbarHostState.showSnackbar("Bluetooth tidak tersedia") }
                                    !adapter.isEnabled -> scope.launch { snackbarHostState.showSnackbar("Nyalakan Bluetooth terlebih dahulu") }
                                    sdk31Plus && !hasBtPermission -> scope.launch { requestBtPermissionLauncher.launch(btPermission) }
                                    activeAddr.isNullOrBlank() -> scope.launch { snackbarHostState.showSnackbar("Pilih printer terlebih dahulu") }
                                    else -> {
                                        scope.launch {
                                            val effective = uiState.settings ?: StoreSettings()
                                            val txId = UUID.randomUUID().toString()
                                            val items = listOf(
                                                TransactionItemEntity(
                                                    transactionId = txId,
                                                    productId = "sku-TEST",
                                                    productName = "Test Item",
                                                    productPrice = 1000.0,
                                                    quantity = 1,
                                                    unitPrice = 1000.0,
                                                    subtotal = 1000.0
                                                )
                                            )
                                            val tx = TransactionEntity(
                                                transactionNumber = "TEST-${System.currentTimeMillis()}",
                                                cashierId = "test",
                                                cashierName = "Tester",
                                                paymentMethod = PaymentMethod.CASH,
                                                subtotal = 1000.0,
                                                tax = 0.0,
                                                total = 1000.0,
                                                cashReceived = 1000.0,
                                                cashChange = 0.0
                                            )
                                            ESCPosPrinter.printReceipt(context, effective, tx, items)
                                            snackbarHostState.showSnackbar("Perintah cetak dikirim")
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Print, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Test Cetak ESC/POS")
                            }
                        }

                        HorizontalDivider()
                        if (adapter == null) {
                            Text("Bluetooth tidak tersedia di perangkat ini", color = MaterialTheme.colorScheme.error)
                        } else if (!adapter.isEnabled) {
                            Text("Bluetooth nonaktif. Aktifkan Bluetooth untuk memilih printer.", color = MaterialTheme.colorScheme.error)
                        } else if (!hasBtPermission && sdk31Plus) {
                            Text("Izin BLUETOOTH_CONNECT diperlukan untuk melihat perangkat.")
                            OutlinedButton(onClick = { requestBtPermissionLauncher.launch(btPermission) }) {
                                Icon(Icons.Default.Bluetooth, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Izinkan Bluetooth")
                            }
                        } else if (bonded.isEmpty()) {
                            Text("Tidak ada perangkat Bluetooth yang terpasang.")
                            Text("Silakan pasangkan printer terlebih dahulu melalui pengaturan Bluetooth.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            bonded.forEach { device ->
                                val name = device.name ?: "Unknown"
                                val addr = device.address
                                val selected = selectedAddress == addr
                                OutlinedButton(
                                    onClick = {
                                        selectedAddress = addr
                                        selectedName = name
                                        val current = uiState.settings ?: StoreSettings()
                                        viewModel.onEvent(StoreSettingsUiEvent.Save(
                                            current.copy(
                                                printerName = name,
                                                printerAddress = addr,
                                                updatedAt = System.currentTimeMillis()
                                            )
                                        ))
                                        scope.launch { snackbarHostState.showSnackbar("Printer di-set ke $name") }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(name)
                                            Text(addr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (selected) Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
