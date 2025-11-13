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
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.model.ProductFilter

/**
 * Filter Dialog untuk product list
 */
@Composable
fun ProductFilterDialog(
    currentFilter: ProductFilter,
    categories: List<Category>,
    onFilterChanged: (ProductFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryId by remember { mutableStateOf(currentFilter.categoryId) }
    var inStockOnly by remember { mutableStateOf(currentFilter.inStockOnly) }
    var lowStockOnly by remember { mutableStateOf(currentFilter.lowStockOnly) }
    var activeOnly by remember { mutableStateOf(currentFilter.activeOnly) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Produk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Filter
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleSmall
                )

                Column(modifier = Modifier.selectableGroup()) {
                    // All categories option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategoryId == null,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Semua Kategori")
                    }

                    // Individual categories
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategoryId == category.id,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    }
                }

                Divider()

                // Stock Filters
                Text(
                    text = "Stok",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = inStockOnly,
                        onCheckedChange = { inStockOnly = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hanya yang tersedia")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = lowStockOnly,
                        onCheckedChange = { lowStockOnly = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stok menipis")
                }

                Divider()

                // Active Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = activeOnly,
                        onCheckedChange = { activeOnly = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hanya produk aktif")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onFilterChanged(
                        ProductFilter(
                            categoryId = selectedCategoryId,
                            inStockOnly = inStockOnly,
                            lowStockOnly = lowStockOnly,
                            activeOnly = activeOnly
                        )
                    )
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

