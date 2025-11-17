package id.stargan.intikasir.feature.reports.domain.usecase

import id.stargan.intikasir.feature.reports.domain.model.ProfitLossReport
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetProfitLossReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long): ProfitLossReport {
        return repository.getProfitLossReport(startDate, endDate)
    }
}

