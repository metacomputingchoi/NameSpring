// utils/analysis/ReportHelper.kt
package com.ssc.namespring.utils.analysis

import com.ssc.namespring.utils.data.json.JsonDataRepository

internal class ReportHelper(private val repository: JsonDataRepository) {

    fun getReportSectionTitle(section: String): String {
        // reportTemplates는 lateinit이므로 직접 접근
        return repository.reportTemplates.sectionTitles[section] ?: section
    }

    fun getReportSubsectionLabel(subsection: String): String {
        return repository.reportTemplates.subsectionLabels[subsection] ?: subsection
    }
}