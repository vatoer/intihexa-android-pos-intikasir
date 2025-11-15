package id.stargan.intikasir.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    leftButtonMode: LeftButtonMode = LeftButtonMode.Disable,
    onDelete: (() -> Unit)? = null,
    enabled: Boolean = true,
    showTextField: Boolean = true,
    step: Int = 1
) {
    val haptic = LocalHapticFeedback.current
    var internalText by rememberSaveable(value) { mutableStateOf(value.toString()) }
    var isUserEditing by remember { mutableStateOf(false) }

    fun commitText() {
        val parsed = internalText.toIntOrNull()
        if (parsed != null && parsed in min..max) {
            if (parsed != value) onValueChange(parsed)
            internalText = parsed.toString()
        } else {
            // Revert
            internalText = value.toString()
        }
        isUserEditing = false
    }

    fun updateValue(newValue: Int) {
        val bounded = newValue.coerceIn(min, max)
        if (bounded != value) {
            onValueChange(bounded)
            internalText = bounded.toString()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics { contentDescription = "Stepper" }
    ) {
        // Left Button (Minus / Delete / Disabled)
        val canDecrease = value > min
        when {
            value == min && leftButtonMode == LeftButtonMode.Delete -> {
                IconButton(
                    onClick = {
                        onDelete?.invoke() ?: onValueChange(-1)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    enabled = enabled,
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus item")
                }
            }
            else -> {
                IconButton(
                    onClick = { if (enabled) updateValue(value - step) },
                    enabled = enabled && canDecrease,
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Kurangi")
                }
            }
        }

        // Numeric TextField (optional)
        if (showTextField) {
            TextField(
                value = internalText,
                onValueChange = { newText ->
                    internalText = newText.filter { it.isDigit() }.take(6)
                    isUserEditing = true
                },
                modifier = Modifier
                    .width(72.dp)
                    .padding(horizontal = 8.dp),
                singleLine = true,
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    commitText()
                }),
                isError = internalText.toIntOrNull()?.let { it !in min..max } == true,
                supportingText = {
                    if (internalText.toIntOrNull()?.let { it !in min..max } == true) {
                        Text("${min}-${max}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        } else {
            // Display only
            Text(
                text = value.toString(),
                modifier = Modifier
                    .width(48.dp)
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Plus Button
        val canIncrease = value < max
        IconButton(
            onClick = { if (enabled) updateValue(value + step) },
            enabled = enabled && canIncrease,
            modifier = Modifier.semantics { role = Role.Button }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Tambah")
        }
    }

    // Commit on focus loss (if user typed but not pressed Done)
    LaunchedEffect(value) {
        if (!isUserEditing) internalText = value.toString()
    }
}

enum class LeftButtonMode {
    Delete, Disable
}
