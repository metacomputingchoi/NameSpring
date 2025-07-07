// ui/compare/filter/ScoreRangeFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namingengine.data.GeneratedName

class ScoreRangeFilter(
    private val minScore: Int,
    private val maxScore: Int
) : NameFilter {

    private val gson = Gson()

    override fun matches(favorite: FavoriteName): Boolean {
        return try {
            val generatedName = gson.fromJson(favorite.jsonData, GeneratedName::class.java)
            val score = generatedName.analysisInfo?.totalScore ?: 0
            score in minScore..maxScore
        } catch (e: Exception) {
            false
        }
    }
}