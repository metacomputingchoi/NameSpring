// model/domain/service/report/interfaces/IReportSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IReportSectionGenerator {
    fun generateSection(profile: Profile): Map<String, Any>
    fun getSectionName(): String
}