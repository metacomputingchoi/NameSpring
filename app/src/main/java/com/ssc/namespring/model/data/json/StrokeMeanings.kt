// model/data/json/StrokeMeanings.kt
package com.ssc.namespring.model.data.json

import com.google.gson.annotations.SerializedName

data class StrokeMeanings(
    @SerializedName("stroke_meanings")
    val strokeMeanings: Map<String, StrokeMeaningDetail>
)

data class StrokeMeaningDetail(
    val number: Int,
    val title: String,
    val summary: String,

    @SerializedName("detailed_explanation")
    val detailedExplanation: String,

    @SerializedName("positive_aspects")
    val positiveAspects: String,

    @SerializedName("caution_points")
    val cautionPoints: String,

    @SerializedName("personality_traits")
    val personalityTraits: List<String>,

    @SerializedName("suitable_career")
    val suitableCareer: List<String>,

    @SerializedName("life_period_influence")
    val lifePeriodInfluence: String,

    @SerializedName("special_characteristics")
    val specialCharacteristics: String?,

    @SerializedName("challenge_period")
    val challengePeriod: String?,

    @SerializedName("opportunity_area")
    val opportunityArea: String?,

    @SerializedName("lucky_level")
    val luckyLevel: String  // 최상운수, 상운수, 양운수, 흉운수, 최흉운수
) {
    /**
     * lucky_level을 점수로 변환 (0-100)
     */
    fun getLuckyScore(): Int {
        return when (luckyLevel) {
            "최상운수" -> 100
            "상운수" -> 85
            "양운수" -> 65
            "흉운수" -> 30
            "최흉운수" -> 0
            else -> 50  // 기본값
        }
    }

    /**
     * lucky_level을 5단계 등급으로 변환
     */
    fun getLuckyGrade(): String {
        return when (luckyLevel) {
            "최상운수" -> "A+"
            "상운수" -> "A"
            "양운수" -> "B"
            "흉운수" -> "D"
            "최흉운수" -> "F"
            else -> "C"
        }
    }

    /**
     * lucky_level에 따른 색상 코드 (UI용)
     */
    fun getLuckyColorCode(): String {
        return when (luckyLevel) {
            "최상운수" -> "#FFD700"  // 금색
            "상운수" -> "#4ECDC4"    // 청록색
            "양운수" -> "#45B7D1"    // 파란색
            "흉운수" -> "#FFA500"    // 주황색
            "최흉운수" -> "#FF6B6B"  // 빨간색
            else -> "#95A5A6"        // 회색
        }
    }
}