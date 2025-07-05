// ui/profilelist/ProfileListViewHolder.kt
package com.ssc.namespring.ui.profilelist

import android.util.Log
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.R

class ProfileListViewHolder(private val activity: ProfileListActivity) {

    lateinit var toolbar: Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var emptyView: LinearLayout
    lateinit var fabAdd: FloatingActionButton
    lateinit var fabSelectAll: ExtendedFloatingActionButton
    lateinit var searchView: SearchView
    lateinit var chipGroup: ChipGroup
    lateinit var tvProfileCount: TextView
    lateinit var tvSelectedCount: TextView
    lateinit var bottomActionBar: LinearLayout
    lateinit var progressBar: ProgressBar

    fun initViews() {
        Log.d("ProfileListActivity", "initViews() called")

        toolbar = activity.findViewById(R.id.toolbar)
        recyclerView = activity.findViewById(R.id.recyclerView)
        emptyView = activity.findViewById(R.id.emptyView)
        fabAdd = activity.findViewById(R.id.fabAdd)
        fabSelectAll = activity.findViewById(R.id.fabSelectAll)
        searchView = activity.findViewById(R.id.searchView)
        chipGroup = activity.findViewById(R.id.chipGroup)
        tvProfileCount = activity.findViewById(R.id.tvProfileCount)
        tvSelectedCount = activity.findViewById(R.id.tvSelectedCount)
        bottomActionBar = activity.findViewById(R.id.bottomActionBar)
        progressBar = activity.findViewById(R.id.progressBar)

        Log.d("ProfileListActivity", "Basic views initialized")

        activity.setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }
}