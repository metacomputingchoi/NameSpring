// model/domain/service/report/generators/BasicInfoSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.generators

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator
import java.text.SimpleDateFormat
import java.util.*

class BasicInfoSectionGenerator : IReportSectionGenerator {

    override fun getSectionName(): String = "basicInfo"

    override fun generateSection(profile: Profile): Map<String, Any> {
        return mapOf(
            "profileName" to profile.profileName,
            "fullName" to buildFullName(profile),
            "fullNameHanja" to buildFullNameHanja(profile),
            "birthDate" to formatBirthDate(profile),
            "birthTime" to formatBirthTime(profile),
            "isYajaTime" to profile.isYajaTime,
            "createdDate" to formatDate(profile.createdAt),
            "lastUpdated" to formatDate(profile.updatedAt)
        )
    }

    private fun buildFullName(profile: Profile): String {
        return "${profile.surname?.korean ?: ""}${profile.givenName?.korean ?: ""}"
    }

    private fun buildFullNameHanja(profile: Profile): String {
        return "${profile.surname?.hanja ?: ""}${profile.givenName?.hanja ?: ""}"
    }

    private fun formatBirthDate(profile: Profile): String {
        return SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
            .format(profile.birthDate.time)
    }

    private fun formatBirthTime(profile: Profile): String {
        return SimpleDateFormat("HH시 mm분", Locale.KOREAN)
            .format(profile.birthDate.time)
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(timestamp))
    }
}