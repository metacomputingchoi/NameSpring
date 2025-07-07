// ui/history/helpers/HistorySearchHelper.kt
package com.ssc.namespring.ui.history.helpers

import android.content.Context
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import com.ssc.namespring.R

object HistorySearchHelper {
    fun setupSearchView(context: Context, searchView: SearchView, onQueryChanged: (String) -> Unit) {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                onQueryChanged(newText ?: "")
                return true
            }
        })
    }
}