// ui/compare/CompareSearchManager.kt
package com.ssc.namespring.ui.compare

import android.content.Context
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import com.ssc.namespring.R

class CompareSearchManager(
    private val context: Context,
    private val searchView: SearchView,
    private val viewModel: CompareViewModel
) {
    fun setupSearch() {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }
}