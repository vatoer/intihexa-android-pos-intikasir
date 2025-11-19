package id.stargan.intikasir.ui.common.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Subtle notification banner that appears briefly without blocking UI
 * Better UX alternative to Toast/Snackbar
 */
@Composable
fun SubtleNotification(
    message: String,
    icon: ImageVector = Icons.Default.CheckCircle,
    type: NotificationType = NotificationType.Success,
    modifier: Modifier = Modifier
) {
    val containerColor = when (type) {
        NotificationType.Success -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        NotificationType.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
        NotificationType.Info -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
    }

    val contentColor = when (type) {
        NotificationType.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        NotificationType.Error -> MaterialTheme.colorScheme.onErrorContainer
        NotificationType.Info -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

enum class NotificationType {
    Success,
    Error,
    Info
}

/**
 * Composable host for subtle notifications
 * Shows notification briefly at top of screen without blocking interaction
 */
@Composable
fun BoxScope.SubtleNotificationHost(
    state: SubtleNotificationState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        state.currentMessage?.let { msg ->
            SubtleNotification(
                message = msg.text,
                icon = msg.icon,
                type = msg.type
            )
        }
    }
}

/**
 * State holder for subtle notifications
 */
class SubtleNotificationState {
    var isVisible by mutableStateOf(false)
        private set

    var currentMessage by mutableStateOf<NotificationMessage?>(null)
        private set

    suspend fun show(
        message: String,
        icon: ImageVector = Icons.Default.CheckCircle,
        type: NotificationType = NotificationType.Success,
        duration: Long = 2000L
    ) {
        currentMessage = NotificationMessage(message, icon, type)
        isVisible = true
        delay(duration)
        isVisible = false
        delay(300) // Wait for animation to finish
        currentMessage = null
    }
}

data class NotificationMessage(
    val text: String,
    val icon: ImageVector,
    val type: NotificationType
)

@Composable
fun rememberSubtleNotificationState(): SubtleNotificationState {
    return remember { SubtleNotificationState() }
}

