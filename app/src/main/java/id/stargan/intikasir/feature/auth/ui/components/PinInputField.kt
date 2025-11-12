package id.stargan.intikasir.feature.auth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom PIN input field component
 * Menampilkan PIN dengan dots dan support backspace
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinInputField(
    pin: String,
    onPinChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    maxLength: Int = 6,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Dots Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            repeat(maxLength) { index ->
                PinDot(
                    isFilled = index < pin.length,
                    isError = isError
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hidden TextField for keyboard input
        OutlinedTextField(
            value = pin,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength && newValue.all { it.isDigit() }) {
                    onPinChanged(newValue)
                }
            },
            enabled = enabled,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            modifier = Modifier
                .width(0.dp)
                .height(0.dp),
            singleLine = true
        )

        // Number Pad (Custom)
        NumberPad(
            onNumberClick = { number ->
                if (pin.length < maxLength) {
                    onPinChanged(pin + number)
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    onPinChanged(pin.dropLast(1))
                }
            },
            onClearClick = onClear,
            enabled = enabled
        )
    }
}

/**
 * Single PIN dot indicator
 */
@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        isFilled -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFilled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    )
}

/**
 * Custom number pad untuk input PIN
 */
@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: 1, 2, 3
        NumberPadRow(
            numbers = listOf("1", "2", "3"),
            onNumberClick = onNumberClick,
            enabled = enabled
        )

        // Row 2: 4, 5, 6
        NumberPadRow(
            numbers = listOf("4", "5", "6"),
            onNumberClick = onNumberClick,
            enabled = enabled
        )

        // Row 3: 7, 8, 9
        NumberPadRow(
            numbers = listOf("7", "8", "9"),
            onNumberClick = onNumberClick,
            enabled = enabled
        )

        // Row 4: Clear, 0, Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            // Clear button
            TextButton(
                onClick = onClearClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }

            // 0 button
            NumberButton(
                number = "0",
                onClick = { onNumberClick("0") },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            // Backspace button
            IconButton(
                onClick = onBackspaceClick,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Row of number buttons
 */
@Composable
private fun NumberPadRow(
    numbers: List<String>,
    onNumberClick: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        numbers.forEach { number ->
            NumberButton(
                number = number,
                onClick = { onNumberClick(number) },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single number button
 */
@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

