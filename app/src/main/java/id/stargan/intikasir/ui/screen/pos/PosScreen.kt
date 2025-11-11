package id.stargan.intikasir.ui.screen.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.CartItem
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product
import java.util.Locale

/**
 * Main POS Screen dengan 2 panel: Product Grid (kiri) dan Cart (kanan)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    modifier: Modifier = Modifier
) {
    // Untuk demo, menggunakan dummy state
    // Nanti akan di-connect dengan ViewModel
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inti Kasir - POS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left Panel: Product Grid (70%)
            ProductPanel(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onProductClick = { product ->
                    // Add to cart
                    val existingItem = cartItems.find { it.productId == product.id }
                    if (existingItem != null) {
                        val index = cartItems.indexOf(existingItem)
                        cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
                    } else {
                        cartItems.add(
                            CartItem(
                                productId = product.id,
                                productName = product.name,
                                productPrice = product.price,
                                productSku = product.sku,
                                quantity = 1
                            )
                        )
                    }
                }
            )

            // Right Panel: Cart (30%)
            CartPanel(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                cartItems = cartItems,
                onQuantityChange = { item, newQuantity ->
                    if (newQuantity <= 0) {
                        cartItems.remove(item)
                    } else {
                        val index = cartItems.indexOf(item)
                        cartItems[index] = item.copy(quantity = newQuantity)
                    }
                },
                onRemoveItem = { item ->
                    cartItems.remove(item)
                },
                onCheckout = {
                    // Handle checkout
                }
            )
        }
    }
}

@Composable
private fun ProductPanel(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    onProductClick: (Product) -> Unit
) {
    // Dummy data untuk demo
    val dummyCategories = listOf(
        Category("1", "Makanan", null, "#FF6B6B", "🍔", 0, 0),
        Category("2", "Minuman", null, "#4ECDC4", "🥤", 0, 0),
        Category("3", "Snack", null, "#FFE66D", "🍿", 0, 0)
    )

    val dummyProducts = listOf(
        Product("1", "Nasi Goreng", "Nasi goreng spesial", 15000.0, categoryId = "1", categoryName = "Makanan", createdAt = 0, updatedAt = 0),
        Product("2", "Mie Goreng", "Mie goreng pedas", 12000.0, categoryId = "1", categoryName = "Makanan", createdAt = 0, updatedAt = 0),
        Product("3", "Es Teh Manis", "Teh manis dingin", 3000.0, categoryId = "2", categoryName = "Minuman", createdAt = 0, updatedAt = 0),
        Product("4", "Kopi", "Kopi hitam", 5000.0, categoryId = "2", categoryName = "Minuman", createdAt = 0, updatedAt = 0),
        Product("5", "Keripik", "Keripik kentang", 8000.0, categoryId = "3", categoryName = "Snack", createdAt = 0, updatedAt = 0)
    )

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Category Filter
        CategoryFilter(
            categories = dummyCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Product Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(dummyProducts) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Cari produk...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun CategoryFilter(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.height(120.dp)) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Semua") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory?.id == category.id,
                onClick = { onCategorySelected(category) },
                label = { Text("${category.icon ?: ""} ${category.name}") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            Text(
                text = product.formattedPrice,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Stock indicator
            if (product.trackStock) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stok: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.isLowStock) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CartPanel(
    modifier: Modifier = Modifier,
    cartItems: List<CartItem>,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onCheckout: () -> Unit
) {
    val subtotal = cartItems.sumOf { it.subtotal }
    val tax = subtotal * 0.1 // 10% PPN
    val total = subtotal + tax

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        // Cart Title
        Text(
            text = "Keranjang (${cartItems.size} item)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cart Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cartItems, key = { it.productId }) { item ->
                CartItemCard(
                    item = item,
                    onQuantityChange = { newQuantity ->
                        onQuantityChange(item, newQuantity)
                    },
                    onRemove = { onRemoveItem(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary
        CartSummary(
            subtotal = subtotal,
            tax = tax,
            total = total
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Checkout Button
        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = cartItems.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BAYAR", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, "Decrease")
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }

                // Subtotal
                Text(
                    text = item.formattedSubtotal(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CartSummary(
    subtotal: Double,
    tax: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    fun formatCurrency(amount: Double) = "Rp ${String.format(Locale("id", "ID"), "%,.0f", amount)}"

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryRow("Subtotal", formatCurrency(subtotal))
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRow("Pajak (10%)", formatCurrency(tax))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TOTAL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatCurrency(total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

