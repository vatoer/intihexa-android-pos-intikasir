package id.stargan.intikasir.feature.reports.domain.usecase

import id.stargan.intikasir.feature.reports.domain.model.ReportDashboard
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long, cashierId: String? = null): ReportDashboard {
        return repository.getDashboardData(startDate, endDate, cashierId)
    }
}
