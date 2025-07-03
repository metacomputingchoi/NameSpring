// model/data/Profile.kt
package com.ssc.namespring.model.data

import android.util.Log
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.io.Serializable
import java.util.Calendar

data class Profile(
    val id: String = System.currentTimeMillis().toString(),
    var profileName: String,
    var birthDate: Calendar,
    var isYajaTime: Boolean = false,
    var surname: SurnameInfo? = null,
    var givenName: GivenNameInfo? = null,
    var nameBomScore: Int = 0,
    var sajuInfo: SajuInfo? = null,
    var ohaengInfo: OhaengInfo? = null,
    var evaluatedName: GeneratedName? = null,
    val nameCharCount: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    fun updateFromGeneratedName(generatedName: GeneratedName) {
        evaluatedName = generatedName
        nameBomScore = calculateNamebomScore(generatedName)

        generatedName.analysisInfo?.let { analysisInfo ->
            sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)
            ohaengInfo = extractOhaengInfo(analysisInfo)
        }

        updatedAt = System.currentTimeMillis()
        Log.d("Profile", "updateFromGeneratedName - 점수: $nameBomScore, 오행: $ohaengInfo")
    }

    private fun calculateNamebomScore(generatedName: GeneratedName): Int {
        val analysisInfo = generatedName.analysisInfo ?: return 0
        val scoreBreakdown = analysisInfo.scoreBreakdown

        Log.d("Profile", "=== 점수 계산 시작 ===")
        Log.d("Profile", "총점(totalScore): ${analysisInfo.totalScore}")
        Log.d("Profile", "받은 점수들:")
        scoreBreakdown.forEach { (category, score) ->
            Log.d("Profile", "  $category: $score")
        }

        var ohaengBonus = 0
        try {
            val jawonOhaeng = analysisInfo.ohaengInfo.jawonOhaeng
            val missingElements = analysisInfo.sajuInfo.missingElements
            val dominantElements = analysisInfo.sajuInfo.dominantElements
            val sajuOhaengCount = analysisInfo.sajuInfo.sajuOhaengCount

            Log.d("Profile", "자원오행: $jawonOhaeng")
            Log.d("Profile", "부족한 오행: $missingElements")
            Log.d("Profile", "과다한 오행: $dominantElements")
            Log.d("Profile", "사주 오행 분포: $sajuOhaengCount")

            jawonOhaeng.forEach { element ->
                if (missingElements.contains(element)) {
                    ohaengBonus += 10
                    Log.d("Profile", "부족한 오행 $element 보완 -> +10점")
                } else if (dominantElements.contains(element)) {
                    ohaengBonus -= 5
                    Log.d("Profile", "과다한 오행 $element 추가 -> -5점")
                } else {
                    val count = sajuOhaengCount[element] ?: 0
                    if (count <= 1) {
                        ohaengBonus += 5
                        Log.d("Profile", "적은 오행 $element 보완 -> +5점")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Profile", "자원오행 분석 실패", e)
        }

        val baseScore = (analysisInfo.totalScore * 100 / 160).coerceIn(0, 100)
        val ohaengScore = scoreBreakdown["오행조화"] ?: 0
        if (ohaengScore == 0) {
            ohaengBonus = (ohaengBonus * 1.5).toInt()
            Log.d("Profile", "오행조화 점수가 0이므로 자원오행 보너스 1.5배 적용")
        }

        val finalScore = (baseScore + ohaengBonus).coerceIn(0, 100)

        Log.d("Profile", "기본 점수: $baseScore")
        Log.d("Profile", "자원오행 보너스: $ohaengBonus")
        Log.d("Profile", "최종 점수: $finalScore")
        Log.d("Profile", "=== 점수 계산 끝 ===")

        return finalScore
    }

    private fun extractOhaengInfo(analysisInfo: NameAnalysisInfo): OhaengInfo {
        val sajuOhaengCount = analysisInfo.sajuInfo.sajuOhaengCount
        return OhaengInfo(
            wood = sajuOhaengCount["木"] ?: 0,
            fire = sajuOhaengCount["火"] ?: 0,
            earth = sajuOhaengCount["土"] ?: 0,
            metal = sajuOhaengCount["金"] ?: 0,
            water = sajuOhaengCount["水"] ?: 0
        )
    }

    fun getFullName(): String {
        val surnameText = surname?.korean ?: ""
        val givenNameText = givenName?.korean ?: ""
        return "$surnameText$givenNameText"
    }

    fun getFullNameWithHanja(): String {
        val surnameHanja = surname?.hanja ?: ""
        val givenNameHanja = givenName?.hanja ?: ""
        val korean = getFullName()
        return if (surnameHanja.isNotEmpty() || givenNameHanja.isNotEmpty()) {
            "$korean($surnameHanja$givenNameHanja)"
        } else {
            korean
        }
    }

    fun getBirthDateString(): String {
        val year = birthDate.get(Calendar.YEAR)
        val month = birthDate.get(Calendar.MONTH) + 1
        val day = birthDate.get(Calendar.DAY_OF_MONTH)
        val hour = birthDate.get(Calendar.HOUR_OF_DAY)
        val minute = birthDate.get(Calendar.MINUTE)

        val hourText = if (hour < 12) "오전 ${hour}시" else "오후 ${hour - 12}시"
        return "${year}년 ${month}월 ${day}일 $hourText ${minute}분생"
    }

    fun getSimpleBirthDate(): String {
        val year = birthDate.get(Calendar.YEAR)
        val month = birthDate.get(Calendar.MONTH) + 1
        val day = birthDate.get(Calendar.DAY_OF_MONTH)
        return "${year}년 ${month}월 ${day}일생"
    }

    fun getBirthTimeString(): String {
        val hour = birthDate.get(Calendar.HOUR_OF_DAY)
        val minute = birthDate.get(Calendar.MINUTE)
        return String.format("%02d시 %02d분", hour, minute)
    }

    fun getScoreThemeColor(): ScoreTheme {
        val hasCompleteName = givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        if (surname != null && hasCompleteName && nameBomScore > 0) {
            return when (nameBomScore) {
                in 80..100 -> ScoreTheme.SUNNY_SPRING
                in 60..79 -> ScoreTheme.WARM_SPRING
                in 40..59 -> ScoreTheme.CLOUDY_SPRING
                in 20..39 -> ScoreTheme.RAINY_SPRING
                else -> ScoreTheme.COLD_SPRING
            }
        }

        return ScoreTheme.NOT_EVALUATED
    }

    fun isEvaluated(): Boolean {
        val hasCompleteName = givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        return surname != null && hasCompleteName && nameBomScore > 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false

        return profileName == other.profileName &&
                birthDate.timeInMillis == other.birthDate.timeInMillis &&
                surname?.korean == other.surname?.korean &&
                surname?.hanja == other.surname?.hanja
    }

    override fun hashCode(): Int {
        var result = profileName.hashCode()
        result = 31 * result + birthDate.timeInMillis.hashCode()
        result = 31 * result + (surname?.korean?.hashCode() ?: 0)
        result = 31 * result + (surname?.hanja?.hashCode() ?: 0)
        return result
    }

    enum class ScoreTheme {
        SUNNY_SPRING,
        WARM_SPRING,
        CLOUDY_SPRING,
        RAINY_SPRING,
        COLD_SPRING,
        NOT_EVALUATED
    }
}

data class SurnameInfo(
    val korean: String,
    val hanja: String,
    val meaning: String? = null,
    val strokes: Int = 0,
    val ohaeng: String? = null,
    val eumyang: Int = 0
) : Serializable

data class GivenNameInfo(
    val korean: String,
    val hanja: String,
    val charInfos: List<CharInfo> = emptyList()
) : Serializable

data class CharInfo(
    val korean: String,
    val hanja: String,
    val meaning: String? = null,
    val strokes: Int = 0,
    val ohaeng: String? = null,
    val eumyang: Int = 0
) : Serializable

data class OhaengInfo(
    val wood: Int = 0,
    val fire: Int = 0,
    val earth: Int = 0,
    val metal: Int = 0,
    val water: Int = 0
) : Serializable {
    fun getLackingOhaeng(): List<String> {
        val ohaengMap = mapOf(
            "목" to wood,
            "화" to fire,
            "토" to earth,
            "금" to metal,
            "수" to water
        )
        return ohaengMap.filter { it.value == 0 }.keys.toList()
    }

    fun getExcessOhaeng(): List<String> {
        val ohaengMap = mapOf(
            "목" to wood,
            "화" to fire,
            "토" to earth,
            "금" to metal,
            "수" to water
        )
        val total = wood + fire + earth + metal + water
        if (total == 0) return emptyList()

        val avg = total / 5.0
        return ohaengMap.filter { it.value > avg * 1.5 && it.value >= 3 }.keys.toList()
    }

    override fun toString(): String {
        return "OhaengInfo(목=$wood, 화=$fire, 토=$earth, 금=$metal, 수=$water)"
    }
}
