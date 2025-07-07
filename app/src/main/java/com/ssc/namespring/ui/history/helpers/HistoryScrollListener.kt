// ui/history/helpers/HistoryScrollListener.kt
package com.ssc.namespring.ui.history.helpers

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HistoryScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val fab: FloatingActionButton,
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // FAB 표시/숨김
        if (dy > 0 && fab.visibility == View.VISIBLE) {
            fab.hide()
        } else if (dy < 0 && fab.visibility != View.VISIBLE) {
            fab.show()
        }

        // 페이지네이션
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        if (lastVisibleItem >= totalItemCount - 5) {
            onLoadMore()
        }
    }
}