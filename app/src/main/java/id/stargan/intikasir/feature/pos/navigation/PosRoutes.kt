package id.stargan.intikasir.feature.pos.navigation

object PosRoutes {
    const val POS = "pos"
    const val RECEIPT = "receipt"

    fun receiptDetail(transactionId: String) = "receipt/$transactionId"
}


