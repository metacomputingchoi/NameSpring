// utils/json/ReportHelper.kt
package com.ssc.namespring.utils.json

internal class ReportHelper(private val repository: JsonDataRepository) {

    fun getReportSectionTitle(section: String): String {
        return repository.reportTemplates.sectionTitles[section] ?: section
    }

    fun getReportSubsectionLabel(subsection: String): String {
        return repository.reportTemplates.subsectionLabels[subsection] ?: subsection
    }
}