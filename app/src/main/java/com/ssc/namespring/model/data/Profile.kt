// model/data/Profile.kt
package com.ssc.namespring.model.data

import java.time.LocalDateTime

data class Profile(
    val id: String,
    val profileName: String,      // "나", "배우자", "첫째" 등
    val surname: String,          // 성 한글
    val surnameHanja: String,     // 성 한자
    val givenName: String,        // 이름 한글
    val givenNameHanja: String,   // 이름 한자
    val birthDateTime: LocalDateTime,
    val useYajasi: Boolean = false,
    val sajuInfo: SajuInfo? = null,
    val namebomScore: Int = 0,    // 이름봄 점수 (0-100)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getFullName(): String = surname + givenName
    fun getFullHanjaName(): String = surnameHanja + givenNameHanja

    fun getNameLength(): Int = givenName.length
    fun getSurnameLength(): Int = surname.length
    fun getTotalNameLength(): Int = surname.length + givenName.length

    fun getNameInputFormat(): String {
        // NamingEngine 입력 형식으로 변환
        val surnameInput = "[$surname/$surnameHanja]"
        val givenNameInput = givenName.mapIndexed { index, char ->
            val hanja = givenNameHanja.getOrNull(index) ?: "_"
            "[$char/$hanja]"
        }.joinToString("")
        return surnameInput + givenNameInput
    }
}