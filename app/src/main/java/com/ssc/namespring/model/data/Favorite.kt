// model/data/Favorite.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime

data class Favorite(
    val id: String,
    val profileId: String,
    val generatedName: GeneratedName,
    val memo: String? = null,
    val category: String? = null,          // 분류 (예: "후보", "최종", "검토중")
    val rating: Int? = null,               // 별점 (1-5)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getDisplayName(): String = 
        "${generatedName.surnameHangul}${generatedName.combinedPronounciation}"

    fun getDisplayHanja(): String = 
        "${generatedName.surnameHanja}${generatedName.combinedHanja}"

    fun getNamebomScore(): Int = 
        generatedName.analysisInfo?.let { analysisInfo ->
            // totalScore를 100점 만점으로 변환
            // scoreBreakdown을 참고하여 더 정확한 계산
            val breakdown = analysisInfo.scoreBreakdown
            val maxPossibleScore = breakdown.values.sum() // 현재 최대 점수
            val actualScore = analysisInfo.totalScore
            (actualScore * 100 / maxPossibleScore).coerceIn(0, 100)
        } ?: 0

    fun getNameMeaning(): String =
        generatedName.hanjaDetails.joinToString(" + ") { it.inmyongMeaning }

    fun getMainFeatures(): List<String> {
        val features = mutableListOf<String>()

        generatedName.analysisInfo?.let { info ->
            // 음양 균형 특징
            if (info.eumYangInfo.isBalanced) {
                features.add("음양균형")
            }

            // 오행 조화 특징
            if (info.ohaengInfo.overallHarmony.contains("조화")) {
                features.add("오행조화")
            }

            // 사격 길흉 특징
            val goodSagyeokCount = info.scoreBreakdown["사격점수"]?.let { score ->
                when {
                    score >= 80 -> "최상급사격"
                    score >= 60 -> "상급사격"
                    score >= 40 -> "중급사격"
                    else -> null
                }
            }
            goodSagyeokCount?.let { features.add(it) }

            // 사주 보완 특징
            info.sajuInfo.missingElements.takeIf { it.isNotEmpty() }?.let {
                if (info.recommendations.any { rec -> rec.contains("보완") }) {
                    features.add("사주보완")
                }
            }
        }

        return features
    }
}