// model/Profile.kt
package com.ssc.namespring.model

import java.io.Serializable
import java.util.Calendar

data class Profile(
    val id: String = System.currentTimeMillis().toString(),
    var profileName: String,
    var birthDate: Calendar,
    var isYajaTime: Boolean = false, // 야자시 처리 옵션
    var surname: SurnameInfo? = null,
    var givenName: GivenNameInfo? = null,
    var nameBomScore: Int = 0,
    var sajuInfo: SajuInfo? = null,
    var ohaengInfo: OhaengInfo? = null,
    val nameCharCount: Int = 2
) : Serializable {

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

data class SajuInfo(
    val yearPillar: String,
    val monthPillar: String,
    val dayPillar: String,
    val hourPillar: String
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
        val avg = (wood + fire + earth + metal + water) / 5.0
        return ohaengMap.filter { it.value > avg * 1.5 }.keys.toList()
    }
}