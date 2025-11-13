package id.stargan.intikasir.feature.product.navigation

/**
 * Navigation routes untuk Product feature
 */
object ProductRoutes {
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_FORM = "product_form"
    const val PRODUCT_FORM_WITH_ID = "product_form/{productId}"
    const val CATEGORY_MANAGEMENT = "category_management"

    fun productFormWithId(productId: String) = "product_form/$productId"

    const val PRODUCT_ADD = "product/add"
    const val PRODUCT_EDIT = "product/edit/{productId}"
    const val CATEGORY_MANAGE = "categories/manage"

    private const val PRODUCT_DETAIL_ROUTE = "product/detail"
    const val PRODUCT_DETAIL = "$PRODUCT_DETAIL_ROUTE/{productId}"

    fun productDetail(productId: String): String {
        return "$PRODUCT_DETAIL_ROUTE/$productId"
    }
}

const val PRODUCT_GRAPH_ROUTE = "product_graph"

