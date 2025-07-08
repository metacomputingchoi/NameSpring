// ui/compare/adapter/CompareTargetAdapter.kt
package com.ssc.namespring.ui.compare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName

class CompareTargetAdapter(
    private val onRemoveClick: (FavoriteName) -> Unit
) : ListAdapter<FavoriteName, CompareTargetAdapter.CompareViewHolder>(DiffCallbackTarget()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compare_target, parent, false)
        return CompareViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompareViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class CompareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val tvOrder: TextView = itemView.findViewById(R.id.tvOrder)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(favorite: FavoriteName, order: Int) {
            tvOrder.text = order.toString()
            tvName.text = "${favorite.fullNameKorean} (${favorite.fullNameHanja})"

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

            btnRemove.setOnClickListener {
                onRemoveClick(favorite)
            }

            // 카드 클릭 시 순서 변경 등의 기능을 추가할 수 있음
            cardView.setOnLongClickListener {
                // 드래그앤드롭 구현 가능
                true
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

    class DiffCallbackTarget : DiffUtil.ItemCallback<FavoriteName>() {
        override fun areItemsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem.getKey() == newItem.getKey()
        }

        override fun areContentsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem == newItem
        }
    }
}