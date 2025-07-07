// model/domain/service/report/utils/ReportMetadataProvider.kt
package com.ssc.namespring.model.domain.service.report.utils

class ReportMetadataProvider {

    fun createWorkResult(
        success: Boolean,
        report: Map<String, Any>? = null,
        error: String? = null
    ): Map<String, Any> {
        return if (success && report != null) {
            mapOf(
                "success" to true,
                "data" to createSuccessData(report)
            )
        } else {
            mapOf(
                "success" to false,
                "error" to (error ?: "Unknown error occurred")
            )
        }
    }

    private fun createSuccessData(report: Map<String, Any>): Map<String, Any> {
        val sections = report["sections"] as? Map<*, *> ?: emptyMap<Any, Any>()

        return mapOf(
            "report" to report,
            "reportId" to (report["reportId"] as? String ?: ""),
            "format" to (report["format"] as? String ?: ""),
            "sectionCount" to sections.size,
            "timestamp" to System.currentTimeMillis()
        )
    }
}