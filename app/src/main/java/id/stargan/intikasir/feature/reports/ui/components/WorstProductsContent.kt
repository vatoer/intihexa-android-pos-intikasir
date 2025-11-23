package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.ProductInfo
import id.stargan.intikasir.feature.reports.domain.model.ProductSales
import id.stargan.intikasir.feature.reports.domain.model.WorstProductsReport

@Composable
fun WorstProductsContent(
    report: WorstProductsReport?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Worst Selling Products", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Worst products (10 produk teratas dengan jumlah terjual paling sedikit)", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (report?.worstProducts.isNullOrEmpty()) {
            Text(text = "Tidak ada produk yang terjual pada periode ini")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(report.worstProducts) { ps ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${ps.productName} (${ps.productId})")
                        Text(text = "${ps.quantitySold} pcs")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Not sold (produk tidak terjual)", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (report?.notSold.isNullOrEmpty()) {
            Text(text = "Semua produk terjual")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(report.notSold) { p ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${p.productName} (${p.productId})")
                        Text(text = "Stok: ${p.stock}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorstProductsPreview() {
    val report = WorstProductsReport(
        worstProducts = listOf(ProductSales("p1", "Produk A", 2, 20000.0)),
        notSold = listOf(ProductInfo("p2", "Produk B", 10))
    )
    MaterialTheme {
        WorstProductsContent(report = report)
    }
}
