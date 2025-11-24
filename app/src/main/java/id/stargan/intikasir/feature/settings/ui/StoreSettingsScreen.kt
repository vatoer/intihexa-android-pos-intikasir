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
import id.stargan.intikasir.feature.settings.ui.components.LogoSection
import id.stargan.intikasir.feature.settings.ui.components.PrintingSettingsSection
import id.stargan.intikasir.feature.settings.ui.components.BluetoothPrinterPickerSection
import id.stargan.intikasir.feature.security.ui.SecuritySettingsViewModel
import id.stargan.intikasir.feature.security.util.usePermission
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.home.navigation.HistoryRoleViewModel
import id.stargan.intikasir.domain.model.UserRole
import androidx.compose.runtime.collectAsState

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
        // Permission checks for editing settings
        val securityVm: SecuritySettingsViewModel = hiltViewModel()
        val canEditSettings = usePermission(securityVm.observePermission("CASHIER") { it.canEditSettings })
        val getCurrentUserUseCase: GetCurrentUserUseCase = hiltViewModel<HistoryRoleViewModel>().getCurrentUserUseCase
        val currentUser by getCurrentUserUseCase().collectAsState(initial = null)
        val isAdmin = currentUser?.role == UserRole.ADMIN
        val isEditable = isAdmin || canEditSettings

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo Section (extracted)
                LogoSection(
                    logoPreviewUri = uiState.logoPreviewUri,
                    isImageProcessing = uiState.isImageProcessing,
                    onPickFromGallery = onPickLogo,
                    onCaptureWithCameraRequested = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (granted) onCaptureLogo() else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onRemoveLogo = { viewModel.onEvent(StoreSettingsUiEvent.RemoveLogo) },
                    isEditable = isEditable
                )

                // Store Info Section (Editable)
                StoreInfoSection(
                    settings = uiState.settings ?: StoreSettings(),
                    onSave = { updated -> viewModel.onEvent(StoreSettingsUiEvent.Save(updated)) },
                    isEditable = isEditable
                )

                // Receipt Settings Section (Header/Footer)
                ReceiptSettingsSection(
                    settings = uiState.settings ?: StoreSettings(),
                    onSave = { updated -> viewModel.onEvent(StoreSettingsUiEvent.Save(updated)) },
                    isEditable = isEditable
                )

                // Printing Settings Section (extracted)
                PrintingSettingsSection(
                    settings = uiState.settings ?: StoreSettings(),
                    onSave = { updated -> viewModel.onEvent(StoreSettingsUiEvent.Save(updated)) },
                    isEditable = isEditable
                )

                // Bluetooth Printer Picker Section (extracted)
                BluetoothPrinterPickerSection(
                    adapter = BluetoothAdapter.getDefaultAdapter(),
                    hasBtPermissions = hasBtPermissions,
                    bluetoothPermissions = bluetoothPermissions,
                    onRequestPermissions = { perms -> requestBtPermissionsLauncher.launch(perms) },
                    settings = uiState.settings,
                    onSavePrinter = { name, addr ->
                        val current = uiState.settings ?: StoreSettings()
                        viewModel.onEvent(
                            StoreSettingsUiEvent.Save(
                                current.copy(
                                    printerName = name,
                                    printerAddress = addr,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        )
                        scope.launch { snackbarHostState.showSnackbar("Printer di-set ke $name") }
                    },
                    isEditable = isEditable
                )

                // Activation Info Section
                ActivationInfoCard()
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
