// ui/history/managers/NameListSearchManager.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import com.ssc.namespring.R
import com.ssc.namespring.utils.search.NameSearchHelper

class NameListSearchManager(
    private val onSearchQueryChanged: (String) -> Unit
) {
    companion object {
        private const val TAG = "NameListSearchManager"
    }

    private val searchHelper = NameSearchHelper()

    fun setupSearchView(searchView: SearchView) {
        val searchEditText = searchView.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        searchEditText?.apply {
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText ?: ""
                Log.d(TAG, "Search query changed: $query")
                onSearchQueryChanged(query)
                return true
            }
        })
    }

    fun getSearchHelper() = searchHelper
}