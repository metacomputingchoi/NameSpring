// ui/history/adapter/NameListAdapter.kt
package com.ssc.namespring.ui.history.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namingengine.data.GeneratedName

class NameListAdapter(
    private val birthDateTime: String,
    private val birthDateTimeMillis: Long,
    private val onNameClick: (GeneratedName) -> Unit,
    private val favoriteRepository: FavoriteNameRepository,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<GeneratedName, NameListAdapter.NameViewHolder>(NameDiffCallback()) {

    companion object {
        private const val TAG = "NameListAdapter"
    }

    private val gson = Gson()

    init {
        favoriteRepository.favorites.observe(lifecycleOwner) { favorites ->
            Log.d(TAG, "Favorites updated: ${favorites.size} items")
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_name_result, parent, false)
        return NameViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBirthDateTime: TextView = itemView.findViewById(R.id.tvBirthDateTime)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val btnDetail: MaterialButton = itemView.findViewById(R.id.btnDetail)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        // NameListAdapter.kt - bind 메서드 수정
        fun bind(name: GeneratedName, rank: Int) {
            tvRank.text = "#$rank"

            // 전체 이름 = 성씨 + 이름
            val fullNameKorean = "${name.surnameHangul}${name.combinedPronounciation}"
            val fullNameHanja = "${name.surnameHanja}${name.combinedHanja}"

            tvName.text = "$fullNameKorean ($fullNameHanja)"
            tvBirthDateTime.text = birthDateTime

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

            // 즐겨찾기 상태 확인 - 전체 이름으로
            val isFavorite = favoriteRepository.isFavorite(
                birthDateTimeMillis,
                fullNameKorean,
                fullNameHanja
            )

            Log.d(TAG, "Binding $fullNameKorean: favorite=$isFavorite")
            updateFavoriteIcon(isFavorite)

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
            // 전체 이름 구성
            val fullNameKorean = "${name.surnameHangul}${name.combinedPronounciation}"
            val fullNameHanja = "${name.surnameHanja}${name.combinedHanja}"

            // 이름 부분은 이미 분리되어 있음
            val givenNameKorean = name.combinedPronounciation
            val givenNameHanja = name.combinedHanja

            Log.d(TAG, "Creating favorite:")
            Log.d(TAG, "  Full name: $fullNameKorean ($fullNameHanja)")
            Log.d(TAG, "  Surname: ${name.surnameHangul} (${name.surnameHanja})")
            Log.d(TAG, "  Given name: $givenNameKorean ($givenNameHanja)")

            val favorite = FavoriteName(
                birthDateTime = birthDateTimeMillis,
                nameKorean = givenNameKorean,
                nameHanja = givenNameHanja,
                surnameKorean = name.surnameHangul,
                surnameHanja = name.surnameHanja,
                jsonData = gson.toJson(name)
            )

            Log.d(TAG, "Favorite key will be: ${favorite.getKey()}")

            // 현재 상태 확인
            val currentlyFavorite = favoriteRepository.isFavorite(
                birthDateTimeMillis,
                fullNameKorean,
                fullNameHanja
            )

            Log.d(TAG, "Toggle favorite: $fullNameKorean, current: $currentlyFavorite")

            // 즉시 UI 업데이트
            updateFavoriteIcon(!currentlyFavorite)

            // 애니메이션 효과
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

            // Repository 업데이트
            favoriteRepository.toggleFavorite(favorite)
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            btnFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )

            // 색상 틴트는 XML에서 이미 설정되어 있으므로 제거
            // (ic_star_filled.xml에 이미 #FFD700 색상이 적용됨)
        }
    }

    class NameDiffCallback : DiffUtil.ItemCallback<GeneratedName>() {
        override fun areItemsTheSame(oldItem: GeneratedName, newItem: GeneratedName): Boolean {
            return oldItem.combinedHanja == newItem.combinedHanja
        }

        override fun areContentsTheSame(oldItem: GeneratedName, newItem: GeneratedName): Boolean {
            return oldItem == newItem
        }
    }
}