// model/application/service/report/analyzer/BaseAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer

import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

abstract class BaseAnalyzer {
    protected val templates = ReportDataHolder.reportTemplatesData
    protected val formatSettings = ReportDataHolder.formatSettingsData

    protected fun getSectionTitle(key: String): String {
        return templates.sectionTitles[key] ?: ""
    }

    protected fun getSubsectionLabel(key: String): String {
        return templates.subsectionLabels[key] ?: ""
    }

    protected fun formatListItems(items: List<String>, separator: String = ", "): String {
        return items.joinToString(separator)
    }
}