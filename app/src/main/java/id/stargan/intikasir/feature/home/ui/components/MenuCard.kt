package id.stargan.intikasir.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.home.domain.model.MenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.tooling.preview.Preview

/**
 * Menu Card Item untuk home screen
 * - icon shown inside a circular tinted background
 * - label below, centered
 */
@Composable
fun MenuCard(
    menuItem: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple deterministic palette to tint icon backgrounds; keep tones subtle
    val palette = listOf(
        Color(0xFFE3F2FD), // light blue
        Color(0xFFE8F5E9), // light green
        Color(0xFFFFF3E0), // light orange
        Color(0xFFF3E5F5), // light purple
        Color(0xFFFFEBEE), // light red
        Color(0xFFE0F7FA), // cyan
        Color(0xFFFFFDE7), // yellow
        Color(0xFFEDE7F6)  // lavender
    )
    val idx = (menuItem.id.hashCode().absoluteValue) % palette.size
    val bgColor = palette[idx]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(Modifier),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        // clickable area handled inside so we get ripple; keep visual flat (no border)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon circle (enlarged)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = menuItem.icon,
                        contentDescription = menuItem.title,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary // keep icon color aligned with theme
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = menuItem.title,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description removed per UX request — keep grid compact and aligned
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuCardPreview() {
    MaterialTheme {
        MenuCard(
            menuItem = MenuItem(
                id = "cashier",
                title = "Kasir",
                icon = Icons.Default.PointOfSale,
                route = "cashier"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuGridPreview() {
    val items = listOf(
        MenuItem("1", "Kasir", Icons.Default.PointOfSale, "cashier"),
        MenuItem("2", "Produk", Icons.Default.ShoppingBag, "products"),
        MenuItem("3", "Kategori", Icons.Default.Category, "categories"),
    )

    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            for (item in items) {
                Box(modifier = Modifier.width(120.dp)) {
                    MenuCard(menuItem = item, onClick = {})
                }
            }
        }
    }
}

// helper to get absolute value without importing kotlin.math at top-level
private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this
