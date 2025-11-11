package id.stargan.intikasir.domain.model

/**
 * Domain model untuk item dalam keranjang belanja
 */
data class CartItem(
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productSku: String? = null,
    val quantity: Int = 1,
    val discount: Double = 0.0
) {
    val subtotal: Double
        get() = (productPrice * quantity) - discount

    fun formattedSubtotal(): String = formatCurrency(subtotal)

    fun formattedPrice(): String = formatCurrency(productPrice)

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }
}

