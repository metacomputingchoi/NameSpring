// ui/history/components/NameListViewComponentsHolder.kt
package com.ssc.namespring.ui.history.adapter.components

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namingengine.data.GeneratedName

class NameListViewComponentsHolder(
    itemView: View,
    private val birthDateTime: String,
    private val birthDateTimeMillis: Long,
    private val onNameClick: (GeneratedName) -> Unit,
    private val favoriteHandler: NameListFavoriteHandler
) : RecyclerView.ViewHolder(itemView) {

    private val cardView: CardView = itemView.findViewById(R.id.cardView)
    private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
    private val tvName: TextView = itemView.findViewById(R.id.tvName)
    private val tvBirthDateTime: TextView = itemView.findViewById(R.id.tvBirthDateTime)
    private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
    private val btnDetail: MaterialButton = itemView.findViewById(R.id.btnDetail)
    private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

    fun bind(name: GeneratedName, rank: Int) {
        setupBasicInfo(name, rank)
        setupScore(name)
        setupFavoriteButton(name)
        setupClickListeners(name)
    }

    private fun setupBasicInfo(name: GeneratedName, rank: Int) {
        tvRank.text = "#$rank"

        val fullNameKorean = "${name.surnameHangul}${name.combinedPronounciation}"
        val fullNameHanja = "${name.surnameHanja}${name.combinedHanja}"

        tvName.text = "$fullNameKorean ($fullNameHanja)"
        tvBirthDateTime.text = birthDateTime
    }

    private fun setupScore(name: GeneratedName) {
        val score = name.analysisInfo?.totalScore ?: 0
        tvScore.text = "${score}점"

        tvScore.setTextColor(
            when {
                score >= 90 -> itemView.context.getColor(R.color.score_excellent)
                score >= 80 -> itemView.context.getColor(R.color.score_good)
                score >= 70 -> itemView.context.getColor(R.color.score_average)
                else -> itemView.context.getColor(R.color.score_below)
            }
        )
    }

    private fun setupFavoriteButton(name: GeneratedName) {
        val fullNameKorean = "${name.surnameHangul}${name.combinedPronounciation}"
        val fullNameHanja = "${name.surnameHanja}${name.combinedHanja}"

        val isFavorite = favoriteHandler.isFavorite(
            birthDateTimeMillis,
            fullNameKorean,
            fullNameHanja
        )

        updateFavoriteIcon(isFavorite)
    }

    private fun setupClickListeners(name: GeneratedName) {
        btnFavorite.setOnClickListener {
            handleFavoriteClick(name)
        }

        btnDetail.setOnClickListener {
            onNameClick(name)
        }

        cardView.setOnClickListener {
            onNameClick(name)
        }
    }

    private fun handleFavoriteClick(name: GeneratedName) {
        val isCurrentlyFavorite = favoriteHandler.toggleFavorite(
            name = name,
            birthDateTimeMillis = birthDateTimeMillis
        )

        updateFavoriteIcon(!isCurrentlyFavorite)
        animateFavoriteButton()
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_star_filled
            else R.drawable.ic_star_outline
        )
    }

    private fun animateFavoriteButton() {
        btnFavorite.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                btnFavorite.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}