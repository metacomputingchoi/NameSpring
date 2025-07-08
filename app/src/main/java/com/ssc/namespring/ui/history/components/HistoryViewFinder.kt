// ui/history/components/HistoryViewFinder.kt
package com.ssc.namespring.ui.history.components

import android.app.Activity
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.R

class HistoryViewFinder(private val activity: Activity) {
    val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
    val tabLayout: TabLayout = activity.findViewById(R.id.tabLayout)
    val chipGroup: ChipGroup = activity.findViewById(R.id.chipGroup)
    val recyclerView: RecyclerView = activity.findViewById(R.id.recyclerView)
    val emptyView: TextView = activity.findViewById(R.id.emptyView)
    val searchView: SearchView = activity.findViewById(R.id.searchView)
    val sortSpinner: Spinner = activity.findViewById(R.id.sortSpinner)
    val fabScrollTop: FloatingActionButton = activity.findViewById(R.id.fabScrollTop)
    val selectionModeButtons: LinearLayout = activity.findViewById(R.id.selectionModeButtons)
}