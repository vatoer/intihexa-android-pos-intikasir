package id.stargan.intikasir.feature.settings.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import java.io.File

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

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
