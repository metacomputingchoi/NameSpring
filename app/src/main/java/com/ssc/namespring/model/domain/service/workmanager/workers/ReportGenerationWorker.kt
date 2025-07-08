// model/domain/service/workmanager/workers/ReportGenerationWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.domain.service.report.builder.ReportBuilder
import com.ssc.namespring.model.domain.service.report.generators.*
import com.ssc.namespring.model.domain.service.report.utils.ReportMetadataProvider
import com.ssc.namespring.model.domain.service.workmanager.WorkResult
import kotlinx.coroutines.delay

class ReportGenerationWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {

    private val reportBuilder: ReportBuilder by lazy {
        ReportBuilder(createSectionGenerators())
    }

    private val metadataProvider = ReportMetadataProvider()

    override suspend fun performWork(): WorkResult {
        return try {
            val result = generateReport()
            WorkResult(
                success = result["success"] as Boolean,
                data = result["data"] as? Map<String, Any>,
                error = result["error"] as? String
            )
        } catch (e: Exception) {
            WorkResult(
                success = false,
                error = "Report generation failed: ${e.message}"
            )
        }
    }

    private suspend fun generateReport(): Map<String, Any> {
        val inputData = getInputDataMap()
        val reportParams = extractReportParameters(inputData)

        updateProgress(10)

        val profile = getProfile() ?: return metadataProvider.createWorkResult(
            success = false,
            error = "Profile not found for report generation"
        )

        updateProgress(20)

        // Build report with progress updates
        val report = buildReportWithProgress(profile, reportParams)

        // Simulate report compilation
        delay(1000)
        updateProgress(100)

        return metadataProvider.createWorkResult(
            success = true,
            report = report
        )
    }

    private suspend fun buildReportWithProgress(
        profile: com.ssc.namespring.model.domain.entity.Profile,
        params: ReportParameters
    ): Map<String, Any> {
        // Progress will be updated by the builder internally
        val progressSteps = mapOf(
            30 to "기본 정보 생성",
            40 to "사주 분석",
            50 to "이름 평가",
            60 to "오행 분석",
            80 to "상세 분석"
        )

        progressSteps.forEach { (progress, _) ->
            updateProgress(progress)
            delay(100) // Small delay for progress visibility
        }

        return reportBuilder.buildReport(
            profile = profile,
            reportType = params.reportType,
            format = params.format,
            includeDetails = params.includeDetails
        )
    }

    private fun extractReportParameters(inputData: Map<String, Any?>): ReportParameters {
        return ReportParameters(
            reportType = inputData["reportType"] as? String ?: "comprehensive",
            format = inputData["format"] as? String ?: "JSON",
            includeDetails = inputData["includeDetails"] as? Boolean ?: true
        )
    }

    private suspend fun getProfile(): com.ssc.namespring.model.domain.entity.Profile? {
        val profileManager = ProfileManagerProvider.getInstance()
        return profileManager.getProfile(profileId)
    }

    private fun createSectionGenerators(): List<com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator> {
        return listOf(
            BasicInfoSectionGenerator(),
            SajuSectionGenerator(),
            EvaluationSectionGenerator(),
            OhaengSectionGenerator(),
            DetailedAnalysisSectionGenerator()
        )
    }

    private data class ReportParameters(
        val reportType: String,
        val format: String,
        val includeDetails: Boolean
    )
}