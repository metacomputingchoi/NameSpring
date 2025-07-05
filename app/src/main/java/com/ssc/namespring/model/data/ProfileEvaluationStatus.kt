// model/data/ProfileEvaluationStatus.kt
package com.ssc.namespring.model.data

object ProfileEvaluationStatus {
    fun getScoreThemeColor(profile: Profile): Profile.ScoreTheme {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        if (profile.surname != null && hasCompleteName && profile.nameBomScore > 0) {
            return when (profile.nameBomScore) {
                in 80..100 -> Profile.ScoreTheme.SUNNY_SPRING
                in 60..79 -> Profile.ScoreTheme.WARM_SPRING
                in 40..59 -> Profile.ScoreTheme.CLOUDY_SPRING
                in 20..39 -> Profile.ScoreTheme.RAINY_SPRING
                else -> Profile.ScoreTheme.COLD_SPRING
            }
        }

        return Profile.ScoreTheme.NOT_EVALUATED
    }

    fun isEvaluated(profile: Profile): Boolean {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        return profile.surname != null && hasCompleteName && profile.nameBomScore > 0
    }
}