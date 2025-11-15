package id.stargan.intikasir.feature.pos.navigation

object PosRoutes {
    // Base routes
    const val POS = "pos"
    const val POS_WITH_ID = "pos?transactionId={transactionId}"
    const val CART = "cart/{transactionId}"
    const val PAYMENT = "payment/{transactionId}"
    const val RECEIPT = "receipt/{transactionId}"

    // Navigation helpers
    fun pos(transactionId: String? = null) =
        if (transactionId != null) "pos?transactionId=$transactionId" else "pos"

    fun cart(transactionId: String) = "cart/$transactionId"
    fun payment(transactionId: String) = "payment/$transactionId"
    fun receipt(transactionId: String) = "receipt/$transactionId"

    // Deprecated - keep for backward compatibility
    @Deprecated("Use receipt(transactionId) instead")
    fun receiptDetail(transactionId: String) = receipt(transactionId)
}


