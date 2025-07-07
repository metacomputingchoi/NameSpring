// model/domain/service/report/builder/ReportBuilder.kt
package com.ssc.namespring.model.domain.service.report.builder

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator

class ReportBuilder(
    private val sectionGenerators: List<IReportSectionGenerator>
) {

    fun buildReport(
        profile: Profile,
        reportType: String,
        format: String,
        includeDetails: Boolean
    ): Map<String, Any> {
        val sections = generateSections(profile, includeDetails)

        return mapOf(
            "reportId" to generateReportId(profile),
            "profileId" to profile.id,
            "profileName" to profile.profileName,
            "reportType" to reportType,
            "format" to format,
            "generatedAt" to System.currentTimeMillis(),
            "generatedDate" to formatCurrentDate(),
            "sections" to sections,
            "metadata" to createMetadata(includeDetails)
        )
    }

    private fun generateSections(profile: Profile, includeDetails: Boolean): Map<String, Any> {
        val sections = mutableMapOf<String, Any>()

        sectionGenerators.forEach { generator ->
            val sectionName = generator.getSectionName()

            // Skip detailed analysis if not requested
            if (sectionName == "detailedAnalysis" && !includeDetails) {
                return@forEach
            }

            // Skip sections based on profile data availability
            if (shouldIncludeSection(profile, sectionName)) {
                sections[sectionName] = generator.generateSection(profile)
            }
        }

        return sections
    }

    private fun shouldIncludeSection(profile: Profile, sectionName: String): Boolean {
        return when (sectionName) {
            "sajuAnalysis" -> profile.sajuInfo != null
            "nameEvaluation" -> profile.nameBomScore > 0
            "ohaengAnalysis" -> profile.ohaengInfo != null
            else -> true
        }
    }

    private fun generateReportId(profile: Profile): String {
        return "RPT-${profile.id}-${System.currentTimeMillis()}"
    }

    private fun formatCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun createMetadata(includeDetails: Boolean): Map<String, Any> {
        return mapOf(
            "version" to "1.0",
            "generator" to "NameSpring Report Engine",
            "includesDetails" to includeDetails
        )
    }
}