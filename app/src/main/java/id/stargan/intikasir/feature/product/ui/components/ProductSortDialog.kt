package id.stargan.intikasir.feature.product.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.product.domain.model.ProductSortBy

/**
 * Sort Dialog untuk product list
 */
@Composable
fun ProductSortDialog(
    currentSort: ProductSortBy,
    onSortChanged: (ProductSortBy) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSort by remember { mutableStateOf(currentSort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Urutkan Produk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SortOption(
                    text = "Nama A-Z",
                    selected = selectedSort == ProductSortBy.NAME_ASC,
                    onClick = { selectedSort = ProductSortBy.NAME_ASC }
                )

                SortOption(
                    text = "Nama Z-A",
                    selected = selectedSort == ProductSortBy.NAME_DESC,
                    onClick = { selectedSort = ProductSortBy.NAME_DESC }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SortOption(
                    text = "Harga Termurah",
                    selected = selectedSort == ProductSortBy.PRICE_ASC,
                    onClick = { selectedSort = ProductSortBy.PRICE_ASC }
                )

                SortOption(
                    text = "Harga Termahal",
                    selected = selectedSort == ProductSortBy.PRICE_DESC,
                    onClick = { selectedSort = ProductSortBy.PRICE_DESC }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SortOption(
                    text = "Stok Terendah",
                    selected = selectedSort == ProductSortBy.STOCK_ASC,
                    onClick = { selectedSort = ProductSortBy.STOCK_ASC }
                )

                SortOption(
                    text = "Stok Tertinggi",
                    selected = selectedSort == ProductSortBy.STOCK_DESC,
                    onClick = { selectedSort = ProductSortBy.STOCK_DESC }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SortOption(
                    text = "Terbaru",
                    selected = selectedSort == ProductSortBy.NEWEST,
                    onClick = { selectedSort = ProductSortBy.NEWEST }
                )

                SortOption(
                    text = "Terlama",
                    selected = selectedSort == ProductSortBy.OLDEST,
                    onClick = { selectedSort = ProductSortBy.OLDEST }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSortChanged(selectedSort)
                    onDismiss()
                }
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        modifier = modifier
    )
}

/**
 * Sort Option Item
 */
@Composable
private fun SortOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

