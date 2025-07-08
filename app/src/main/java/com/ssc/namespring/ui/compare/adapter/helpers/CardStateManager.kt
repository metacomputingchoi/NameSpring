// ui/compare/adapter/helpers/CardStateManager.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import com.ssc.namespring.R

/**
 * 카드 뷰의 상태를 관리하는 클래스
 */
class CardStateManager(private val context: Context) {

    fun updateState(
        cardView: MaterialCardView,
        ivActionIcon: ImageView,
        btnFavorite: ImageButton,
        isSelected: Boolean,
        showDeleted: Boolean,
        isDeleted: Boolean
    ) {
        when {
            showDeleted || isDeleted -> resetState(cardView, ivActionIcon, btnFavorite)
            isSelected -> setSelectedState(cardView, ivActionIcon, btnFavorite)
            else -> setUnselectedState(cardView, ivActionIcon, btnFavorite)
        }
    }

    private fun resetState(
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
}