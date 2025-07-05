// ui/profilelist/ProfileListEventHandler.kt
package com.ssc.namespring.ui.profilelist

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.SearchView
import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.R
import com.ssc.namespring.utils.ui.ViewUtils

class ProfileListEventHandler(
    private val activity: ProfileListActivity,
    private val viewHolder: ProfileListViewHolder
) {

    fun setupAllEvents() {
        val navigator = ProfileListNavigator(activity)

        viewHolder.fabAdd.setOnClickListener { navigator.navigateToProfileForm() }
        viewHolder.fabSelectAll.setOnClickListener { activity.listManager.toggleSelectAll() }

        setupDeleteButton()
        setupOtherButtons(navigator)
        setupSearch()
        setupSortChips()
        setupInfiniteScroll()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDeleteButton() {
        Log.d("ProfileListActivity", "Looking for btnDeleteSelected")
        val deleteButton = activity.findViewById<Button>(R.id.btnDeleteSelected)
        if (deleteButton != null) {
            Log.d("ProfileListActivity", "btnDeleteSelected found!")
            Log.d("ProfileListActivity", "Delete button enabled: ${deleteButton.isEnabled}")
            Log.d("ProfileListActivity", "Delete button clickable: ${deleteButton.isClickable}")
            Log.d("ProfileListActivity", "Delete button visibility: ${deleteButton.visibility}")

            val parent = deleteButton.parent as? View
            Log.d("ProfileListActivity", "Parent visibility: ${parent?.visibility}")
            Log.d("ProfileListActivity", "Parent enabled: ${parent?.isEnabled}")

            deleteButton.setOnClickListener {
                Log.d("ProfileListActivity", "Delete button clicked!")
                Log.d("ProfileListActivity", "Button is enabled: ${deleteButton.isEnabled}")
                Log.d("ProfileListActivity", "Button is clickable: ${deleteButton.isClickable}")
                activity.listManager.deleteSelected(activity)
            }

            deleteButton.setOnTouchListener { v, event ->
                Log.d("ProfileListActivity", "Delete button touched! Action: ${event.action}")
                false
            }
        } else {
            Log.e("ProfileListActivity", "btnDeleteSelected is NULL!")
        }
    }

    private fun setupOtherButtons(navigator: ProfileListNavigator) {
        activity.findViewById<Button>(R.id.btnCancelSelection).setOnClickListener {
            activity.listManager.exitSelectionMode()
        }

        activity.findViewById<Button>(R.id.btnCreateProfile).setOnClickListener {
            navigator.navigateToProfileForm()
        }
    }

    private fun setupSearch() {
        viewHolder.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                activity.listManager.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupSortChips() {
        ViewUtils.setupSortChips(viewHolder.chipGroup, activity.layoutInflater) { sortType ->
            activity.listManager.setSortType(sortType)
        }
    }

    private fun setupInfiniteScroll() {
        ViewUtils.setupInfiniteScroll(viewHolder.recyclerView) {
            activity.listManager.loadMoreProfiles()
        }
    }
}