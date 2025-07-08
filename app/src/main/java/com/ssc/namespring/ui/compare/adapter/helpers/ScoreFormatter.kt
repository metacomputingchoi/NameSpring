// ui/compare/adapter/helpers/ScoreFormatter.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.ssc.namespring.R

/**
 * 점수 표시 포맷터
 */
class ScoreFormatter(private val context: Context) {
    private val gson = Gson()

    fun updateScore(tvScore: TextView, jsonData: String) {
        try {
            val generatedName = gson.fromJson(
                jsonData,
                com.ssc.namingengine.data.GeneratedName::class.java
            )
            val score = generatedName.analysisInfo?.totalScore ?: 0
            tvScore.text = "${score}점"
            tvScore.setTextColor(getScoreColor(score))
            tvScore.visibility = View.VISIBLE
        } catch (e: Exception) {
            tvScore.visibility = View.GONE
        }
    }

    private fun getScoreColor(score: Int): Int {
        return context.getColor(
            when {
                score >= 90 -> R.color.score_excellent
                score >= 80 -> R.color.score_good  
                score >= 70 -> R.color.score_average
                else -> R.color.score_below
            }
        )
    }
}