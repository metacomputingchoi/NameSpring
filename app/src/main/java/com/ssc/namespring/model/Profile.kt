// model/Profile.kt
package com.ssc.namespring.model

import android.util.Log
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import com.ssc.namespring.model.data.SajuInfo
import java.io.Serializable
import java.util.Calendar

data class Profile(
    val id: String = System.currentTimeMillis().toString(),
    var profileName: String,
    var birthDate: Calendar,
    var isYajaTime: Boolean = false,
    var surname: SurnameInfo? = null,
    var givenName: GivenNameInfo? = null,

    // mutable 프로퍼티로 변경
    var nameBomScore: Int = 0,
    var sajuInfo: SajuInfo? = null,
    var ohaengInfo: OhaengInfo? = null,
    var evaluatedName: GeneratedName? = null,

    val nameCharCount: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    // GeneratedName에서 정보 추출하는 메서드
    fun updateFromGeneratedName(generatedName: GeneratedName) {
        evaluatedName = generatedName

        // 이름봄 점수 계산
        nameBomScore = calculateNamebomScore(generatedName)

        // 사주 정보 추출
        generatedName.analysisInfo?.let { analysisInfo ->
            sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)

            // 오행 정보 추출
            ohaengInfo = extractOhaengInfo(analysisInfo)
        }

        updatedAt = System.currentTimeMillis()

        Log.d("Profile", "updateFromGeneratedName - 점수: $nameBomScore, 오행: $ohaengInfo")
    }

    private fun calculateNamebomScore(generatedName: GeneratedName): Int {
        val analysisInfo = generatedName.analysisInfo ?: return 0
        val scoreBreakdown = analysisInfo.scoreBreakdown

        val weights = mapOf(
            "사격점수" to 0.3,
            "음양균형" to 0.2,
            "오행조화" to 0.2,
            "획수길흉" to 0.2,
            "발음자연스러움" to 0.1
        )

        var weightedScore = 0.0
        var totalWeight = 0.0

        scoreBreakdown.forEach { (category, score) ->
            val weight = weights[category] ?: 0.1
            val maxScore = when (category) {
                "사격점수" -> 100
                else -> 20
            }

            val normalizedScore = (score.toDouble() / maxScore * 100)
            weightedScore += normalizedScore * weight
            totalWeight += weight
        }

        return if (totalWeight > 0) {
            (weightedScore / totalWeight).toInt().coerceIn(0, 100)
        } else {
            (analysisInfo.totalScore * 100 / 160).coerceIn(0, 100)
        }
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

    // 점수에 따른 테마 색상 반환
    fun getScoreThemeColor(): ScoreTheme {
        // 완전한 이름 정보가 있는지 확인
        val hasCompleteName = givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        // 성씨가 있고 완전한 이름이 있고 점수가 계산된 경우만
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

    // 중복 체크를 위한 equals 재정의
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false

        // 프로필 이름, 생년월일시분, 성씨가 모두 같으면 중복으로 판단
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
        SUNNY_SPRING,   // 80-100
        WARM_SPRING,    // 60-79
        CLOUDY_SPRING,  // 40-59
        RAINY_SPRING,   // 20-39
        COLD_SPRING,    // 0-19
        NOT_EVALUATED   // 평가되지 않음
    }
}

data class SurnameInfo(
    val korean: String,
    val hanja: String,
    val meaning: String? = null,
    val strokes: Int = 0,
    val ohaeng: String? = null,
    val eumyang: Int = 0 // 0: 음, 1: 양
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
        // 평균의 1.5배 이상인 경우를 과다로 판단
        return ohaengMap.filter { it.value > avg * 1.5 && it.value >= 3 }.keys.toList()
    }

    override fun toString(): String {
        return "OhaengInfo(목=$wood, 화=$fire, 토=$earth, 금=$metal, 수=$water)"
    }
}