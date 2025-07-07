// ui/compare/adapter/FavoriteListAdapter.kt
package com.ssc.namespring.ui.compare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import java.text.SimpleDateFormat
import java.util.*

class FavoriteListAdapter(
    private val onItemClick: (FavoriteName) -> Unit,
    private val onFavoriteToggle: (FavoriteName) -> Unit,
    private val onItemLongClick: (FavoriteName) -> Boolean
) : ListAdapter<FavoriteName, FavoriteListAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    private var isSelectionMode = false
    private var selectedItems = emptySet<String>()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)

    fun setSelectionMode(selectionMode: Boolean) {
        if (isSelectionMode != selectionMode) {
            isSelectionMode = selectionMode
            notifyDataSetChanged()
        }
    }

    fun setSelectedItems(items: Set<String>) {
        selectedItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_name, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBirthDateTime: TextView = itemView.findViewById(R.id.tvBirthDateTime)
        private val tvAddedDate: TextView = itemView.findViewById(R.id.tvAddedDate)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(favorite: FavoriteName) {
            // 체크박스 표시
            checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            checkBox.isChecked = selectedItems.contains(favorite.getKey())

            // 이름 정보
            tvName.text = "${favorite.fullNameKorean} (${favorite.fullNameHanja})"

            // 생년월일시
            tvBirthDateTime.text = "생년월일시: ${dateFormat.format(Date(favorite.birthDateTime))}"

            // 추가일 or 삭제일
            if (favorite.isDeleted) {
                tvAddedDate.text = "삭제일: ${dateFormat.format(Date(favorite.deletedAt ?: 0))}"
            } else {
                tvAddedDate.text = "추가일: ${dateFormat.format(Date(favorite.addedAt))}"
            }

            // 즐겨찾기 버튼
            btnFavorite.setImageResource(
                if (favorite.isDeleted) R.drawable.ic_restore
                else R.drawable.ic_star_filled
            )

            btnFavorite.setOnClickListener {
                onFavoriteToggle(favorite)
            }

            // 카드 클릭
            cardView.setOnClickListener {
                if (isSelectionMode) {
                    checkBox.isChecked = !checkBox.isChecked
                }
                onItemClick(favorite)
            }

            cardView.setOnLongClickListener {
                onItemLongClick(favorite)
            }

            // 선택된 상태 표시
            cardView.isSelected = selectedItems.contains(favorite.getKey())
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteName>() {
        override fun areItemsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem.getKey() == newItem.getKey()
        }

        override fun areContentsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem == newItem
        }
    }
}