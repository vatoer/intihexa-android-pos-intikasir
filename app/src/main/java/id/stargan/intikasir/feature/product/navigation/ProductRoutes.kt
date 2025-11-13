package id.stargan.intikasir.feature.product.navigation

/**
 * Navigation routes untuk Product feature
 */
object ProductRoutes {
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val PRODUCT_ADD = "product_add"
    const val PRODUCT_EDIT = "product_edit/{productId}"
    const val CATEGORY_MANAGE = "category_manage"

    fun productDetail(productId: String) = "product_detail/$productId"
    fun productEdit(productId: String) = "product_edit/$productId"
}

