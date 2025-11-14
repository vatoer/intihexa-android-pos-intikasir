package id.stargan.intikasir.feature.product.ui.form.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun BarcodeField(value: String, onScan: () -> Unit, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Barcode") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onScan) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
            }
        }
    )
}

@Composable
fun PriceField(
    value: String,
    error: String?,
    onChange: (formatted: String, raw: String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val raw = input.filter { it.isDigit() }
            val formatted = raw.chunked(3).let {
                if (raw.isEmpty()) "" else raw.reversed().chunked(3).joinToString(".") { it }.reversed()
            }
            onChange(formatted, raw)
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        prefix = { Text("Rp ") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
