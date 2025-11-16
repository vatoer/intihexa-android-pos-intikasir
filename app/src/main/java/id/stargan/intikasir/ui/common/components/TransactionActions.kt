package id.stargan.intikasir.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.TransactionStatus

@Composable
fun TransactionActions(
    status: TransactionStatus?,
    modifier: Modifier = Modifier,
    // Primary actions
    onEdit: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onPrintQueue: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    // Admin destructive
    isAdmin: Boolean = false,
    onDeleteAdmin: (() -> Unit)? = null,
) {
    var completing by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Edit for DRAFT/PENDING
        if ((status == TransactionStatus.DRAFT || status == TransactionStatus.PENDING) && onEdit != null) {
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Edit Transaksi")
            }
        }

        // Complete for PAID
        if (status == TransactionStatus.PAID && onComplete != null) {
            Button(
                onClick = {
                    completing = true
                    onComplete()
                },
                enabled = !completing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Selesai")
            }
        }

        // Row: Cetak & Bagikan
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (onPrint != null) {
                Button(onClick = onPrint, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Cetak")
                }
            }
            if (onShare != null) {
                Button(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Bagikan")
                }
            }
        }

        // Row: Antrian & Hapus (Admin)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (onPrintQueue != null) {
                OutlinedButton(onClick = onPrintQueue, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Receipt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Antrian")
                }
            }
            if (isAdmin && onDeleteAdmin != null) {
                OutlinedButton(
                    onClick = onDeleteAdmin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Hapus (Admin)")
                }
            }
        }
    }
}

