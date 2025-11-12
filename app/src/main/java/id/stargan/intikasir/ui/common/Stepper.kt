package id.stargan.intikasir.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    leftButtonMode: LeftButtonMode = LeftButtonMode.Disable
) {
    val focusManager = LocalFocusManager.current
    var textValue by remember { mutableStateOf(TextFieldValue(value.toString())) }

    // Validasi input
    fun updateValueFromText(text: String) {
        val intValue = text.toIntOrNull()
        if (intValue != null && intValue in min..max) {
            onValueChange(intValue)
            textValue = TextFieldValue(intValue.toString())
        } else {
            textValue = TextFieldValue(value.toString())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Tombol kiri: minus/delete/disable
        when {
            value == min && leftButtonMode == LeftButtonMode.Delete -> {
                IconButton(onClick = { onValueChange(-1) }, enabled = true) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
            value == min && leftButtonMode == LeftButtonMode.Disable -> {
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Filled.Remove, contentDescription = "Minus")
                }
            }
            else -> {
                IconButton(
                    onClick = {
                        val newValue = (value - 1).coerceAtLeast(min)
                        onValueChange(newValue)
                        textValue = TextFieldValue(newValue.toString())
                    },
                    enabled = value > min
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Minus")
                }
            }
        }

        // Input angka
        TextField(
            value = textValue,
            onValueChange = {
                textValue = it
                updateValueFromText(it.text)
            },
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = textValue.text.toIntOrNull()?.let { it < min || it > max } == true,
        )

        // Tombol kanan: plus
        IconButton(
            onClick = {
                val newValue = (value + 1).coerceAtMost(max)
                onValueChange(newValue)
                textValue = TextFieldValue(newValue.toString())
            },
            enabled = value < max
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Plus")
        }
    }
}

enum class LeftButtonMode {
    Delete, Disable
}
