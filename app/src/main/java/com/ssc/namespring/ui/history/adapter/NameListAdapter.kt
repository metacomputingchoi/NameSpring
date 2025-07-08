// ui/history/adapter/NameListAdapter.kt
package com.ssc.namespring.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import com.google.gson.Gson
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.ui.history.adapter.components.NameListDiffCallback
import com.ssc.namespring.ui.history.adapter.components.NameListFavoriteHandler
import com.ssc.namespring.ui.history.adapter.components.NameListViewComponentsHolder
import com.ssc.namingengine.data.GeneratedName

class NameListAdapter(
    private val birthDateTime: String,
    private val birthDateTimeMillis: Long,
    private val onNameClick: (GeneratedName) -> Unit,
    private val favoriteRepository: FavoriteNameRepository,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<GeneratedName, NameListViewComponentsHolder>(NameListDiffCallback()) {

    private val gson = Gson()
    private val favoriteHandler = NameListFavoriteHandler(
        favoriteRepository = favoriteRepository,
        gson = gson
    )

    init {
        favoriteRepository.favorites.observe(lifecycleOwner) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewComponentsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_name_result, parent, false)

        return NameListViewComponentsHolder(
            itemView = view,
            birthDateTime = birthDateTime,
            birthDateTimeMillis = birthDateTimeMillis,
            onNameClick = onNameClick,
            favoriteHandler = favoriteHandler
        )
    }

    override fun onBindViewHolder(holder: NameListViewComponentsHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
}