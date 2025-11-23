package id.stargan.intikasir.feature.reports.domain.usecase

import id.stargan.intikasir.feature.reports.domain.model.WorstProductsReport
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetWorstSellingProductsUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long, cashierId: String? = null, lowThreshold: Int = 5): WorstProductsReport {
        return repository.getWorstSellingProducts(startDate, endDate, cashierId, lowThreshold)
    }
}

