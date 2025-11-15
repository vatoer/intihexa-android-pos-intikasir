package id.stargan.intikasir.feature.product.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.model.ProductFilter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton

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
    var categorySearch by remember { mutableStateOf("") }
    var showCategories by remember { mutableStateOf(true) }
    var minPriceText by remember { mutableStateOf(currentFilter.minPrice?.toInt()?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(currentFilter.maxPrice?.toInt()?.toString() ?: "") }

    val filteredCategories = remember(categorySearch, categories) {
        if (categorySearch.isBlank()) categories else categories.filter { it.name.contains(categorySearch, ignoreCase = true) }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Kategori", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showCategories = !showCategories }) {
                        Text(if (showCategories) "Sembunyikan" else "Tampilkan")
                    }
                }
                if (showCategories) {
                    OutlinedTextField(
                        value = categorySearch,
                        onValueChange = { categorySearch = it },
                        placeholder = { Text("Cari kategori...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Category list scrollable with max height
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
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
                                RadioButton(selected = selectedCategoryId == null, onClick = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Semua Kategori")
                            }
                        }
                        items(filteredCategories, key = { it.id }) { category ->
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
                                RadioButton(selected = selectedCategoryId == category.id, onClick = null)
                                Spacer(Modifier.width(8.dp))
                                Text(category.name)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Stock Filters
                Text(text = "Stok", style = MaterialTheme.typography.titleSmall)
                CheckboxRow(
                    text = "Hanya yang tersedia",
                    checked = inStockOnly,
                    onCheckedChange = { inStockOnly = it }
                )
                CheckboxRow(
                    text = "Stok menipis",
                    checked = lowStockOnly,
                    onCheckedChange = { lowStockOnly = it }
                )


                HorizontalDivider()

                // Active Filter
                Text(text = "Status", style = MaterialTheme.typography.titleSmall)
                CheckboxRow(
                    text = "Hanya produk aktif",
                    checked = activeOnly,
                    onCheckedChange = { activeOnly = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val minPrice = minPriceText.toDoubleOrNull()
                val maxPrice = maxPriceText.toDoubleOrNull()

                onFilterChanged(
                    ProductFilter(
                        categoryId = selectedCategoryId,
                        inStockOnly = inStockOnly,
                        lowStockOnly = lowStockOnly,
                        activeOnly = activeOnly,
                        minPrice = minPrice,
                        maxPrice = maxPrice
                    )
                )
                onDismiss()
            }) { Text("Terapkan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
        modifier = modifier
    )
}

@Composable
private fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = checked,
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
