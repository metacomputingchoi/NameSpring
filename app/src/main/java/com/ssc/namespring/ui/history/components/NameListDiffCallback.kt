// ui/history/components/NameListDiffCallback.kt
package com.ssc.namespring.ui.history.adapter.components

import androidx.recyclerview.widget.DiffUtil
import com.ssc.namingengine.data.GeneratedName

class NameListDiffCallback : DiffUtil.ItemCallback<GeneratedName>() {
    override fun areItemsTheSame(oldItem: GeneratedName, newItem: GeneratedName): Boolean {
        return oldItem.combinedHanja == newItem.combinedHanja
    }

    override fun areContentsTheSame(oldItem: GeneratedName, newItem: GeneratedName): Boolean {
        return oldItem == newItem
    }
}