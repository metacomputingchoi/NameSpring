// presentation/formatter/ReportFormatter.kt
package com.ssc.namespring.presentation.formatter

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.NameReport
import com.ssc.namespring.model.domain.name.value.DetailedNameReport

interface ReportFormatter {
    fun formatBasicReport(name: Name, report: NameReport, scores: NameScore): String
    fun formatDetailedReport(name: Name, report: DetailedNameReport, scores: NameScore): String
}