// ui/compare/CompareDragDropHelper.kt
package com.ssc.namespring.ui.compare

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.ui.compare.adapter.CompareSourceAdapter

class CompareDragDropHelper(
    private val sourceAdapter: CompareSourceAdapter,
    private val viewModel: CompareViewModel
) {
    fun setupDragAndDrop(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favorite = sourceAdapter.currentList[position]
                viewModel.addToComparison(favorite)

                // 스와이프 후 원래 위치로 복원
                sourceAdapter.notifyItemChanged(position)
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                val favorite = sourceAdapter.currentList.getOrNull(position)
                return if (favorite?.isDeleted == true) 0 else super.getSwipeDirs(recyclerView, viewHolder)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}