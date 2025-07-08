// ui/compare/adapter/CompareSourceAdapter.kt
package com.ssc.namespring.ui.compare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.adapter.helpers.CompareSourceViewHolder
import com.ssc.namespring.ui.compare.adapter.helpers.CompareSourceViewHelper

class CompareSourceAdapter(
    private val onItemClick: (FavoriteName) -> Unit,
    private val onFavoriteToggle: (FavoriteName) -> Unit
) : ListAdapter<FavoriteName, CompareSourceViewHolder>(DiffCallbackSource()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareSourceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compare_source, parent, false)
        val viewHelper = CompareSourceViewHelper(parent.context)
        return CompareSourceViewHolder(view, onItemClick, onFavoriteToggle, viewHelper)
    }

    override fun onBindViewHolder(holder: CompareSourceViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = selectedItems.contains(item.getKey())
        holder.bind(item, isSelected, showDeleted)
    }

    class DiffCallbackSource : DiffUtil.ItemCallback<FavoriteName>() {
        override fun areItemsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem.getKey() == newItem.getKey()
        }

        override fun areContentsTheSame(oldItem: FavoriteName, newItem: FavoriteName): Boolean {
            return oldItem == newItem
        }
    }
}