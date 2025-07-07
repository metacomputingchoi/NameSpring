// ui/compare/adapter/CompareSourceAdapter.kt
package com.ssc.namespring.ui.compare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import java.text.SimpleDateFormat
import java.util.*

class CompareSourceAdapter(
    private val onItemClick: (FavoriteName) -> Unit,
    private val onFavoriteToggle: (FavoriteName) -> Unit
) : ListAdapter<FavoriteName, CompareSourceAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)
    private val selectedItems = mutableSetOf<String>()
    private var showDeleted = false

    fun updateSelectedItems(items: Set<String>) {
        selectedItems.clear()
        selectedItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setShowDeleted(show: Boolean) {
        showDeleted = show
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compare_source, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBirthDateTime: TextView = itemView.findViewById(R.id.tvBirthDateTime)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        private val ivActionIcon: ImageView = itemView.findViewById(R.id.ivActionIcon)

        fun bind(favorite: FavoriteName) {
            val isSelected = selectedItems.contains(favorite.getKey())
            val isDeleted = favorite.isDeleted

            // 삭제됨 탭에서는 선택 UI를 표시하지 않음
            if (showDeleted || isDeleted) {
                // 삭제됨 탭이거나 삭제된 항목인 경우
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                cardView.strokeWidth = 0
                ivActionIcon.visibility = View.GONE
                btnFavorite.isEnabled = true
                btnFavorite.alpha = 1.0f
            } else {
                // 즐겨찾기 탭에서 선택 상태에 따른 UI 변경
                if (isSelected) {
                    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.selected_card_background))
                    cardView.strokeWidth = 4
                    cardView.strokeColor = itemView.context.getColor(R.color.primary_blue)
                    ivActionIcon.setImageResource(R.drawable.ic_check_circle)
                    ivActionIcon.visibility = View.VISIBLE
                    ivActionIcon.alpha = 1.0f  // 알파값 1.0으로 설정
                    // 선택된 상태에서는 즐겨찾기 버튼 비활성화
                    btnFavorite.isEnabled = false
                    btnFavorite.alpha = 0.5f
                } else {
                    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                    cardView.strokeWidth = 0
                    ivActionIcon.setImageResource(R.drawable.ic_swipe_right)
                    ivActionIcon.visibility = View.VISIBLE
                    ivActionIcon.alpha = 0.3f  // 스와이프 힌트는 연하게
                    // 선택 해제 시 즐겨찾기 버튼 활성화
                    btnFavorite.isEnabled = true
                    btnFavorite.alpha = 1.0f
                }
            }

            tvName.text = "${favorite.fullNameKorean} (${favorite.fullNameHanja})"
            tvBirthDateTime.text = dateFormat.format(Date(favorite.birthDateTime))

            // 점수 표시
            try {
                val gson = com.google.gson.Gson()
                val generatedName = gson.fromJson(
                    favorite.jsonData,
                    com.ssc.namingengine.data.GeneratedName::class.java
                )
                val score = generatedName.analysisInfo?.totalScore ?: 0
                tvScore.text = "${score}점"
                tvScore.setTextColor(getScoreColor(score))
            } catch (e: Exception) {
                tvScore.visibility = View.GONE
            }

            // 즐겨찾기 버튼 상태
            if (isDeleted) {
                btnFavorite.setImageResource(R.drawable.ic_star_outline)
            } else {
                btnFavorite.setImageResource(R.drawable.ic_star_filled)
            }

            btnFavorite.setOnClickListener {
                // 선택된 상태에서는 동작하지 않음
                if (!isSelected || showDeleted) {
                    onFavoriteToggle(favorite)
                }
            }

            // 카드 전체 클릭 이벤트
            cardView.setOnClickListener {
                // 삭제됨 탭이나 삭제된 항목은 선택 불가
                if (!showDeleted && !isDeleted) {
                    onItemClick(favorite)
                    // 클릭 시 즉시 시각적 피드백
                    it.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction {
                            it.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                }
            }
        }

        private fun getScoreColor(score: Int): Int {
            return itemView.context.getColor(
                when {
                    score >= 90 -> R.color.score_excellent
                    score >= 80 -> R.color.score_good
                    score >= 70 -> R.color.score_average
                    else -> R.color.score_below
                }
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FavoriteName>() {
        override fun areItemsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem.getKey() == newItem.getKey()
        }

        override fun areContentsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem == newItem
        }
    }
}