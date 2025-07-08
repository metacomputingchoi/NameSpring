// ui/history/HistoryActivityMenuHandler.kt
package com.ssc.namespring.ui.history

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.R

class HistoryActivityMenuHandler(
    private val activity: AppCompatActivity,
    private val selectionManager: HistorySelectionManager,
    private val viewModel: HistoryViewModel
) {
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.menu_history, menu)
        menu.findItem(R.id.action_select_mode)?.isVisible = !selectionManager.isSelectionMode
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackPress()
                true
            }
            R.id.action_select_mode -> {
                selectionManager.enterSelectionMode()
                true
            }
            R.id.action_load_all -> {
                viewModel.showLoadAllDialog()
                true
            }
            else -> false
        }
    }

    fun handleBackPress(): Boolean {
        return if (selectionManager.isSelectionMode) {
            selectionManager.exitSelectionMode()
            true
        } else {
            false
        }
    }
}
