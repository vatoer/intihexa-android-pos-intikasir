package id.stargan.intikasir.feature.product.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.domain.model.Category

/**
 * Category Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(CategoryManagementUiEvent.DismissError)
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(CategoryManagementUiEvent.DismissSuccess)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kategori") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(CategoryManagementUiEvent.ShowAddDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
            }
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
        } else if (uiState.categories.isEmpty()) {
            EmptyCategoriesState(
                onAddClick = { viewModel.onEvent(CategoryManagementUiEvent.ShowAddDialog) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryCard(
                        category = category,
                        onEditClick = {
                            viewModel.onEvent(CategoryManagementUiEvent.ShowEditDialog(category))
                        },
                        onDeleteClick = {
                            viewModel.onEvent(CategoryManagementUiEvent.ShowDeleteDialog(category))
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (uiState.showAddDialog || uiState.showEditDialog) {
        CategoryFormDialog(
            isEdit = uiState.showEditDialog,
            name = uiState.categoryName,
            description = uiState.categoryDescription,
            color = uiState.categoryColor,
            icon = uiState.categoryIcon,
            nameError = uiState.nameError,
            isSaving = uiState.isSaving,
            onNameChange = { viewModel.onEvent(CategoryManagementUiEvent.NameChanged(it)) },
            onDescriptionChange = { viewModel.onEvent(CategoryManagementUiEvent.DescriptionChanged(it)) },
            onColorChange = { viewModel.onEvent(CategoryManagementUiEvent.ColorChanged(it)) },
            onIconChange = { viewModel.onEvent(CategoryManagementUiEvent.IconChanged(it)) },
            onSave = { viewModel.onEvent(CategoryManagementUiEvent.SaveCategory) },
            onDismiss = { viewModel.onEvent(CategoryManagementUiEvent.HideDialogs) }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(CategoryManagementUiEvent.HideDialogs) },
            title = { Text("Hapus Kategori") },
            text = {
                Text("Apakah Anda yakin ingin menghapus kategori \"${uiState.selectedCategory?.name}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(CategoryManagementUiEvent.ConfirmDelete) }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(CategoryManagementUiEvent.HideDialogs) }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon/Color
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(category.color ?: "#6200EE"))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon ?: "📦",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!category.description.isNullOrBlank()) {
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCategoriesState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Belum ada kategori",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tambahkan kategori untuk mengorganisir produk",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Kategori")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormDialog(
    isEdit: Boolean,
    name: String,
    description: String,
    color: String,
    icon: String,
    nameError: String?,
    isSaving: Boolean = false,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedIcons = listOf("📦", "🍔", "☕", "🍕", "🥤", "🍰", "🍜", "🍖", "🥗", "🍱")
    val predefinedColors = listOf(
        "#6200EE", "#03DAC5", "#FF0266", "#00C853",
        "#FF6D00", "#2196F3", "#E91E63", "#9C27B0"
    )

    var selectedIconIndex by remember { mutableStateOf(predefinedIcons.indexOf(icon).takeIf { it >= 0 } ?: 0) }
    var selectedColorIndex by remember { mutableStateOf(predefinedColors.indexOf(color).takeIf { it >= 0 } ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Kategori" else "Tambah Kategori") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nama Kategori") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Icon Selection
                Text("Pilih Icon", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predefinedIcons.forEachIndexed { index, iconEmoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (index == selectedIconIndex)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedIconIndex = index
                                    onIconChange(iconEmoji)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = iconEmoji, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                // Color Selection
                Text("Pilih Warna", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predefinedColors.forEachIndexed { index, colorHex ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .clickable {
                                    selectedColorIndex = index
                                    onColorChange(colorHex)
                                }
                        ) {
                            if (index == selectedColorIndex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Menyimpan..." else "Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Batal")
            }
        }
    )
}

