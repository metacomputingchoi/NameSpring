// model/adapter/ProfileFormatter.kt
package com.ssc.namespring.model.adapter

import com.ssc.namespring.model.data.Profile
import java.util.Calendar

class ProfileFormatter {

    fun formatFullName(profile: Profile): String {
        val surname = profile.surname
        val givenName = profile.givenName

        if (surname != null && (givenName == null || givenName.charInfos.isEmpty())) {
            return "${surname.korean}(${surname.hanja}) ◯◯"
        }

        if (surname != null && givenName != null && givenName.charInfos.isNotEmpty()) {
            val givenKorean = givenName.charInfos.joinToString("") {
                it.korean.ifEmpty { "◯" }
            }
            val givenHanja = givenName.charInfos.joinToString("") {
                it.hanja.ifEmpty { "◯" }
            }
            return "${surname.korean}${givenKorean}(${surname.hanja}${givenHanja})"
        }

        return "-"
    }

    fun formatBirthDate(calendar: Calendar): String {
        return String.format("%d.%02d.%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
    }

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