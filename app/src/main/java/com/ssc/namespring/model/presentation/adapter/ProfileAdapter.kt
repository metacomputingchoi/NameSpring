// model/presentation/adapter/ProfileAdapter.kt
package com.ssc.namespring.model.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile

class ProfileAdapter(
    private val onItemClick: (Profile) -> Unit,
    private val onItemLongClick: (Profile) -> Boolean,
    private val onEditClick: (Profile) -> Unit,
    private val onDeleteClick: (Profile) -> Unit,
    private val onDuplicateClick: (Profile) -> Unit
) : RecyclerView.Adapter<ProfileViewHolder>() {

    private val selectionManager = SelectionModeManager()
    private var profiles = listOf<Profile>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Profile>, selectionMode: Boolean = false, selected: Set<String> = setOf()) {
        profiles = list
        selectionManager.setSelectionMode(selectionMode)
        selectionManager.setSelectedIds(selected)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectionMode(enabled: Boolean) {
        selectionManager.setSelectionMode(enabled)
        notifyDataSetChanged()
    }

    fun getSelectableItemCount() = profiles.size
    fun getSelectableIds() = profiles.map { it.id }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_improved_list, parent, false)
        return ProfileViewHolder(
            view,
            onItemClick,
            onItemLongClick,
            ProfileMenuHandler(onEditClick, onDeleteClick, onDuplicateClick)
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position], selectionManager)
    }

    override fun getItemCount() = profiles.size
}