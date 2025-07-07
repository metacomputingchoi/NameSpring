// ui/compare/CompareActivityInitializer.kt
package com.ssc.namespring.ui.compare

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.ui.compare.adapter.CompareSourceAdapter
import com.ssc.namespring.ui.compare.adapter.CompareTargetAdapter

class CompareActivityInitializer(
    private val context: Context,
    private val viewModelStoreOwner: ViewModelStoreOwner
) {
    lateinit var viewModel: CompareViewModel
    lateinit var favoriteRepository: FavoriteNameRepository
    lateinit var sourceAdapter: CompareSourceAdapter
    lateinit var targetAdapter: CompareTargetAdapter

    fun initialize() {
        initViewModel()
        initRepository()
        initAdapters()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(viewModelStoreOwner).get(CompareViewModel::class.java)
    }

    private fun initRepository() {
        favoriteRepository = FavoriteNameRepository.getInstance(context)
    }

    private fun initAdapters() {
        sourceAdapter = CompareSourceAdapter(
            onItemClick = { favorite ->
                viewModel.toggleComparison(favorite)
            },
            onFavoriteToggle = { favorite ->
                if (favorite.isDeleted) {
                    viewModel.restoreFavorite(favorite)
                } else {
                    viewModel.toggleFavoriteStatus(favorite)
                }
            }
        )

        targetAdapter = CompareTargetAdapter(
            onRemoveClick = { favorite ->
                viewModel.removeFromComparison(favorite)
            }
        )
    }
}