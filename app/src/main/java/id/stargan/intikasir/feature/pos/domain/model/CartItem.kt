package id.stargan.intikasir.feature.pos.domain.model

import id.stargan.intikasir.domain.model.Product

/** Cart item representing a product selected for purchase */
data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?,
    val stock: Int,
    val isActive: Boolean,
    val itemDiscount: Double = 0.0
) {
    val subtotal: Double get() = (price * quantity) - itemDiscount.coerceAtMost(price * quantity)
}

fun Product.toCartItem(quantity: Int = 1, itemDiscount: Double = 0.0): CartItem = CartItem(
    productId = id,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    stock = stock,
    isActive = isActive,
    itemDiscount = itemDiscount
)
