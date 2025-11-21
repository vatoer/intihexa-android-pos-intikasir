package id.stargan.intikasir.feature.settings.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.pos.print.ReceiptPrinter
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.PaymentMethod
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.pos.print.ESCPosPrinter
import id.stargan.intikasir.util.BluetoothPermissionHelper
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.activation.ui.ActivationInfoCard
import id.stargan.intikasir.feature.settings.ui.components.ReceiptSettingsSection
import id.stargan.intikasir.feature.settings.ui.components.StoreInfoSection

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
        val destFile = File(context.cacheDir, "logo_crop_${System.currentTimeMillis()}.jpg")
        val dest = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", destFile)
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
        // Grant URI permissions for UCrop to read/write the provided URIs
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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

    // Camera permission launcher - placed after onCaptureLogo so we can call it
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onCaptureLogo()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Izin kamera ditolak") }
        }
    }

    // Bluetooth permissions (CONNECT + SCAN) - request multiple
    val bluetoothPermissions = remember { BluetoothPermissionHelper.getRequiredPermissions() }
    var hasBtPermissions by remember { mutableStateOf(BluetoothPermissionHelper.hasBluetoothPermissions(context)) }
    val requestBtPermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        hasBtPermissions = if (bluetoothPermissions.isEmpty()) {
            true
        } else {
            bluetoothPermissions.all { perm -> results[perm] == true }
        }
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
                // Activation Info Section
                ActivationInfoCard()

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

                        // Logo Preview with single edit pencil overlay
                        var showLogoOptions by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier.size(180.dp)
                        ) {
                            // Inner clipped circle for the image/card only
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
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
                                        onClick = { showLogoOptions = true }
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

                            // FAB overlay outside the clipped child so it won't get cropped
                            SmallFloatingActionButton(
                                onClick = { showLogoOptions = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset((-6).dp, (-6).dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ubah Logo"
                                )
                            }
                        }

                        // Logo options dialog
                        if (showLogoOptions) {
                            AlertDialog(
                                onDismissRequest = { showLogoOptions = false },
                                title = { Text("Pilih Sumber Logo") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Galeri
                                        OutlinedButton(onClick = {
                                            showLogoOptions = false
                                            onPickLogo()
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Default.Image, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Pilih dari Galeri")
                                        }
                                        // Kamera (with runtime permission)
                                        OutlinedButton(onClick = {
                                            showLogoOptions = false
                                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                            if (granted) {
                                                onCaptureLogo()
                                            } else {
                                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Ambil dari Kamera")
                                        }
                                        if (uiState.logoPreviewUri != null) {
                                            OutlinedButton(onClick = {
                                                showLogoOptions = false
                                                viewModel.onEvent(StoreSettingsUiEvent.RemoveLogo)
                                            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )) {
                                                Icon(Icons.Default.Delete, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Hapus Logo")
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showLogoOptions = false }) { Text("Tutup") }
                                }
                            )
                        }

                        Text(
                            text = "Logo akan ditampilkan pada struk penjualan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Store Info Section (Editable)
                StoreInfoSection(
                    settings = uiState.settings ?: StoreSettings(),
                    onSave = { updated -> viewModel.onEvent(StoreSettingsUiEvent.Save(updated)) }
                )

                // Receipt Settings Section (Header/Footer)
                ReceiptSettingsSection(
                    settings = uiState.settings ?: StoreSettings(),
                    onSave = { updated -> viewModel.onEvent(StoreSettingsUiEvent.Save(updated)) }
                )

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
                                        val result = ReceiptPrinter.generateThermalReceiptPdf(context, effective, tx, items)
                                        // Use ShareUtils to share safely
                                        id.stargan.intikasir.util.ShareUtils.shareUri(context, result.pdfUri, "application/pdf", "Bagikan Struk")
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
                    var bonded by remember { mutableStateOf<List<android.bluetooth.BluetoothDevice>>(emptyList()) }
                    var selectedAddress by remember(uiState.settings) { mutableStateOf(uiState.settings?.printerAddress) }
                    var selectedName by remember(uiState.settings) { mutableStateOf(uiState.settings?.printerName) }

                    // refresh bonded list when permissions change or adapter changes
                    LaunchedEffect(adapter, hasBtPermissions) {
                        bonded = if (adapter != null && hasBtPermissions) {
                            try { adapter.bondedDevices.toList() } catch (_: Exception) { emptyList() }
                        } else emptyList()
                    }

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
                                    !hasBtPermissions && bluetoothPermissions.isNotEmpty() -> {
                                        requestBtPermissionsLauncher.launch(bluetoothPermissions)
                                    }
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
                                            // call print with permission already ensured and handle result
                                            when (val result = ESCPosPrinter.printReceipt(context, effective, tx, items)) {
                                                is ESCPosPrinter.PrintResult.Success -> {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Perintah cetak berhasil dikirim",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                is ESCPosPrinter.PrintResult.Error -> {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Gagal mencetak: ${result.message}",
                                                        duration = SnackbarDuration.Long
                                                    )
                                                }
                                            }
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
                        } else if (!hasBtPermissions && bluetoothPermissions.isNotEmpty()) {
                            Text("Izin BLUETOOTH_CONNECT/SCAN diperlukan untuk melihat perangkat.")
                            OutlinedButton(onClick = { requestBtPermissionsLauncher.launch(bluetoothPermissions) }) {
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
