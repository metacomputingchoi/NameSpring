// model/presentation/formatter/ProfileStringFormatter.kt
package com.ssc.namespring.model.presentation.formatter

import android.annotation.SuppressLint
import com.ssc.namespring.model.domain.entity.Profile
import java.util.Calendar

object ProfileStringFormatter {
    fun getFullName(profile: Profile): String {
        val surnameText = profile.surname?.korean ?: ""
        val givenNameText = profile.givenName?.let { givenName ->
            givenName.charInfos.joinToString("") { charInfo ->
                charInfo.korean.ifEmpty { "◯" }
            }
        } ?: ""
        return "$surnameText$givenNameText"
    }

    fun getFullNameWithHanja(profile: Profile): String {
        val surnameHanja = profile.surname?.hanja ?: ""
        val givenNameHanja = profile.givenName?.let { givenName ->
            givenName.charInfos.joinToString("") { charInfo ->
                charInfo.hanja.ifEmpty { "◯" }
            }
        } ?: ""
        val korean = getFullName(profile)
        return if (surnameHanja.isNotEmpty() || givenNameHanja.isNotEmpty()) {
            "$korean($surnameHanja$givenNameHanja)"
        } else {
            korean
        }
    }

    fun getBirthDateString(profile: Profile): String {
        val year = profile.birthDate.get(Calendar.YEAR)
        val month = profile.birthDate.get(Calendar.MONTH) + 1
        val day = profile.birthDate.get(Calendar.DAY_OF_MONTH)
        val hour = profile.birthDate.get(Calendar.HOUR_OF_DAY)
        val minute = profile.birthDate.get(Calendar.MINUTE)

        val hourText = if (hour < 12) "오전 ${hour}시" else "오후 ${hour - 12}시"
        return "${year}년 ${month}월 ${day}일 $hourText ${minute}분생"
    }

    fun getSimpleBirthDate(profile: Profile): String {
        val year = profile.birthDate.get(Calendar.YEAR)
        val month = profile.birthDate.get(Calendar.MONTH) + 1
        val day = profile.birthDate.get(Calendar.DAY_OF_MONTH)
        return "${year}년 ${month}월 ${day}일생"
    }

    @SuppressLint("DefaultLocale")
    fun getBirthTimeString(profile: Profile): String {
        val hour = profile.birthDate.get(Calendar.HOUR_OF_DAY)
        val minute = profile.birthDate.get(Calendar.MINUTE)
        return String.format("%02d시 %02d분", hour, minute)
    }
}