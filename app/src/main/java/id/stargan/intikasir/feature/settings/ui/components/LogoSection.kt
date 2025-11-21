package id.stargan.intikasir.feature.settings.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun LogoSection(
    logoPreviewUri: Uri?,
    isImageProcessing: Boolean,
    onPickFromGallery: () -> Unit,
    onCaptureWithCameraRequested: () -> Unit,
    onRemoveLogo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    if (logoPreviewUri != null) {
                        AsyncImage(
                            model = logoPreviewUri,
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
                                        imageVector = Icons.Default.Image,
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

                    if (isImageProcessing) {
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

            if (showLogoOptions) {
                AlertDialog(
                    onDismissRequest = { showLogoOptions = false },
                    title = { Text("Pilih Sumber Logo") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    showLogoOptions = false
                                    onPickFromGallery()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pilih dari Galeri")
                            }
                            OutlinedButton(
                                onClick = {
                                    showLogoOptions = false
                                    onCaptureWithCameraRequested()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ambil dari Kamera")
                            }
                            if (logoPreviewUri != null) {
                                OutlinedButton(
                                    onClick = {
                                        showLogoOptions = false
                                        onRemoveLogo()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
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
}

