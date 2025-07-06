// model/presentation/formatter/ProfileStringFormatter.kt
package com.ssc.namespring.model.presentation.formatter

import android.annotation.SuppressLint
import com.ssc.namespring.model.domain.entity.Profile
import java.util.Calendar

object ProfileStringFormatter {
    fun getFullName(profile: Profile): String {
        // 성씨 부분: 없으면 "-"
        val surnameText = profile.surname?.korean ?: "-"

        // 이름 부분: charInfo가 있으면 그대로 표시 (빈 값은 ◯)
        val givenNameText = profile.givenName?.let { givenName ->
            if (givenName.charInfos.isNotEmpty()) {
                givenName.charInfos.joinToString("") { charInfo ->
                    charInfo.korean.ifEmpty { "◯" }
                }
            } else {
                ""
            }
        } ?: ""

        return "$surnameText$givenNameText"
    }

    fun getFullNameWithHanja(profile: Profile): String {
        // 한글 이름
        val koreanName = getFullName(profile)

        // 성씨 한자: 성씨가 없으면 "-"
        val surnameHanja = profile.surname?.hanja ?: "-"

        // 이름 한자
        val givenNameHanja = profile.givenName?.let { givenName ->
            if (givenName.charInfos.isNotEmpty()) {
                givenName.charInfos.joinToString("") { charInfo ->
                    charInfo.hanja.ifEmpty { "◯" }
                }
            } else {
                ""
            }
        } ?: ""

        // 한자 정보가 전혀 없으면 한글만 반환
        // (성씨가 없어서 "-"이고 이름 한자도 비어있는 경우)
        if (surnameHanja == "-" && givenNameHanja.isEmpty()) {
            return koreanName
        }

        return "$koreanName($surnameHanja$givenNameHanja)"
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