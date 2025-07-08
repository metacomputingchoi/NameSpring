// ui/compare/adapter/helpers/CompareSourceViewHelper.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * 비교 소스 뷰 헬퍼
 */
open class CompareSourceViewHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)
    private val cardStateManager = CardStateManager(context)
    private val scoreFormatter = ScoreFormatter(context)

    fun updateCardState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton,
        isSelected: Boolean,
        showDeleted: Boolean,
        isDeleted: Boolean
    ) {
        cardStateManager.updateState(
            cardView, ivActionIcon, btnFavorite,
            isSelected, showDeleted, isDeleted
        )
    }

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun updateScore(tvScore: TextView, jsonData: String) {
        scoreFormatter.updateScore(tvScore, jsonData)
    }

    fun animateClick(view: View) {
        ViewAnimator.animateClick(view)
    }
}