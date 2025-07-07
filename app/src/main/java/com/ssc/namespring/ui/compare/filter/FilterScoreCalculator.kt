// ui/compare/filter/FilterScoreCalculator.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.FavoriteNameRepository

class FilterScoreCalculator(context: Context) {

    private val favoriteRepository = FavoriteNameRepository.getInstance(context)
    private val gson = Gson()

    fun calculateScoreRange(): Pair<Int, Int> {
        val favorites = favoriteRepository.getFavoritesList()
        if (favorites.isEmpty()) return DEFAULT_MIN_SCORE to DEFAULT_MAX_SCORE

        var minScore = Int.MAX_VALUE
        var maxScore = Int.MIN_VALUE

        favorites.forEach { favorite ->
            try {
                val generatedName = gson.fromJson(
                    favorite.jsonData,
                    com.ssc.namingengine.data.GeneratedName::class.java
                )
                val score = generatedName.analysisInfo?.totalScore ?: 0
                minScore = minOf(minScore, score)
                maxScore = maxOf(maxScore, score)
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        return if (minScore != Int.MAX_VALUE && maxScore != Int.MIN_VALUE) {
            minScore to maxScore
        } else {
            DEFAULT_MIN_SCORE to DEFAULT_MAX_SCORE
        }
    }

    companion object {
        private const val DEFAULT_MIN_SCORE = 0
        private const val DEFAULT_MAX_SCORE = 100
    }
}