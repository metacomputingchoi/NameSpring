// model/presentation/formatter/ProfileFormatter.kt
package com.ssc.namespring.model.presentation.formatter

import android.annotation.SuppressLint
import com.ssc.namespring.model.domain.entity.Profile
import java.util.Calendar

class ProfileFormatter {

    fun formatFullName(profile: Profile): String {
        val surname = profile.surname
        val givenName = profile.givenName

        // 성씨 부분
        val surnameText = if (surname != null) {
            surname.korean
        } else {
            "-"
        }

        // 이름 부분
        val givenText = if (givenName != null && givenName.charInfos.isNotEmpty()) {
            givenName.charInfos.joinToString("") { it.korean.ifEmpty { "◯" } }
        } else {
            ""
        }

        // 한자 성씨 부분: 성씨가 없으면 "-"
        val surnameHanja = if (surname != null) {
            surname.hanja
        } else {
            "-"
        }

        // 한자 이름 부분
        val givenHanja = if (givenName != null && givenName.charInfos.isNotEmpty()) {
            givenName.charInfos.joinToString("") { it.hanja.ifEmpty { "◯" } }
        } else {
            ""
        }

        // 한자 정보가 있으면 괄호 추가 (성씨가 없어도 이름에 한자가 있을 수 있으므로)
        return if (surnameHanja != "-" || givenHanja.isNotEmpty()) {
            "$surnameText$givenText($surnameHanja$givenHanja)"
        } else {
            "$surnameText$givenText"
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatBirthDate(calendar: Calendar): String {
        return String.format("%d.%02d.%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
    }

    @SuppressLint("DefaultLocale")
    fun formatBirthTime(calendar: Calendar): String {
        return String.format("%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE))
    }

    fun formatOhaengBalance(lacking: List<String>, excessive: List<String>): String {
        return when {
            lacking.isNotEmpty() && excessive.isNotEmpty() ->
                "부족: ${lacking.joinToString(",")} | 과다: ${excessive.joinToString(",")}"
            lacking.isNotEmpty() ->
                "부족한 오행: ${lacking.joinToString(", ")}"
            excessive.isNotEmpty() ->
                "과다한 오행: ${excessive.joinToString(", ")}"
            else -> "오행 균형"
        }
    }
}