// ui/compare/CompareActivityViewBinder.kt
package com.ssc.namespring.ui.compare

import android.app.Activity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.R

class CompareActivityViewBinder(private val activity: Activity) {
    lateinit var toolbar: Toolbar
    lateinit var tabLayout: TabLayout
    lateinit var containerComparison: LinearLayout
    lateinit var containerSource: LinearLayout
    lateinit var containerTarget: LinearLayout
    lateinit var searchView: SearchView
    lateinit var chipGroupFilters: ChipGroup
    lateinit var chipGroupSort: ChipGroup
    lateinit var btnAddFilter: ImageButton
    lateinit var recyclerViewSource: RecyclerView
    lateinit var recyclerViewTarget: RecyclerView
    lateinit var emptyViewSource: TextView
    lateinit var emptyViewTarget: TextView
    lateinit var fabCompare: ExtendedFloatingActionButton

    fun bindViews() {
        toolbar = activity.findViewById(R.id.toolbar)
        tabLayout = activity.findViewById(R.id.tabLayout)
        containerComparison = activity.findViewById(R.id.containerComparison)
        containerSource = activity.findViewById(R.id.containerSource)
        containerTarget = activity.findViewById(R.id.containerTarget)
        searchView = activity.findViewById(R.id.searchView)
        chipGroupFilters = activity.findViewById(R.id.chipGroupFilters)
        chipGroupSort = activity.findViewById(R.id.chipGroupSort)
        btnAddFilter = activity.findViewById(R.id.btnAddFilter)
        recyclerViewSource = activity.findViewById(R.id.recyclerViewSource)
        recyclerViewTarget = activity.findViewById(R.id.recyclerViewTarget)
        emptyViewSource = activity.findViewById(R.id.emptyViewSource)
        emptyViewTarget = activity.findViewById(R.id.emptyViewTarget)
        fabCompare = activity.findViewById(R.id.fabCompare)
    }
}