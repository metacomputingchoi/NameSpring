// ui/compare/adapter/helpers/CompareSourceViewHolder.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import java.util.*

class CompareSourceViewHolder(
    itemView: View,
    private val onItemClick: (FavoriteName) -> Unit,
    private val onFavoriteToggle: (FavoriteName) -> Unit,
    private val viewHelper: CompareSourceViewHelper
) : RecyclerView.ViewHolder(itemView) {

    private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
    private val tvName: TextView = itemView.findViewById(R.id.tvName)
    private val tvBirthDateTime: TextView = itemView.findViewById(R.id.tvBirthDateTime)
    private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
    private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
    private val ivActionIcon: ImageView = itemView.findViewById(R.id.ivActionIcon)

    fun bind(favorite: FavoriteName, isSelected: Boolean, showDeleted: Boolean) {
        val isDeleted = favorite.isDeleted

        // UI 상태 업데이트
        viewHelper.updateCardState(cardView, ivActionIcon, btnFavorite, 
            isSelected, showDeleted, isDeleted)

        // 텍스트 설정
        tvName.text = "${favorite.fullNameKorean} (${favorite.fullNameHanja})"
        tvBirthDateTime.text = viewHelper.formatDate(favorite.birthDateTime)

        // 점수 표시
        viewHelper.updateScore(tvScore, favorite.jsonData)

        // 즐겨찾기 버튼 상태
        btnFavorite.setImageResource(
            if (isDeleted) R.drawable.ic_star_outline 
            else R.drawable.ic_star_filled
        )

        // 이벤트 핸들러
        btnFavorite.setOnClickListener {
            if (!isSelected || showDeleted) {
                onFavoriteToggle(favorite)
            }
        }

        cardView.setOnClickListener {
            if (!showDeleted && !isDeleted) {
                onItemClick(favorite)
                viewHelper.animateClick(it)
            }
        }
    }
}