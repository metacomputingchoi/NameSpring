// ui/compare/adapter/helpers/CompareSourceViewHelper.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.ssc.namespring.R
import java.text.SimpleDateFormat
import java.util.*

open class CompareSourceViewHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)
    private val gson = Gson()

    fun updateCardState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton,
        isSelected: Boolean,
        showDeleted: Boolean,
        isDeleted: Boolean
    ) {
        if (showDeleted || isDeleted) {
            resetCardState(cardView, ivActionIcon, btnFavorite)
        } else {
            if (isSelected) {
                setSelectedState(cardView, ivActionIcon, btnFavorite)
            } else {
                setUnselectedState(cardView, ivActionIcon, btnFavorite)
            }
        }
    }

    private fun resetCardState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton
    ) {
        cardView.setCardBackgroundColor(context.getColor(R.color.white))
        cardView.strokeWidth = 0
        ivActionIcon.visibility = View.GONE
        btnFavorite.isEnabled = true
        btnFavorite.alpha = 1.0f
    }

    private fun setSelectedState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton
    ) {
        cardView.setCardBackgroundColor(context.getColor(R.color.selected_card_background))
        cardView.strokeWidth = 4
        cardView.strokeColor = context.getColor(R.color.primary_blue)
        ivActionIcon.setImageResource(R.drawable.ic_check_circle)
        ivActionIcon.visibility = View.VISIBLE
        ivActionIcon.alpha = 1.0f
        btnFavorite.isEnabled = false
        btnFavorite.alpha = 0.5f
    }

    private fun setUnselectedState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton
    ) {
        cardView.setCardBackgroundColor(context.getColor(R.color.white))
        cardView.strokeWidth = 0
        ivActionIcon.setImageResource(R.drawable.ic_swipe_right)
        ivActionIcon.visibility = View.VISIBLE
        ivActionIcon.alpha = 0.3f
        btnFavorite.isEnabled = true
        btnFavorite.alpha = 1.0f
    }

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

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

    fun animateClick(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}