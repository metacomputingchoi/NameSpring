// model/presentation/components/search/HanjaSearchQueryHandler.kt
package com.ssc.namespring.model.presentation.components.search

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.ssc.namespring.R
import kotlinx.coroutines.*

internal class HanjaSearchQueryHandler(
    private val searchScope: CoroutineScope,
    private val onSearch: suspend (String) -> Unit
) {
    private var searchJob: Job? = null

    fun setupSearchListener(etSearch: EditText) {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchJob?.cancel()
                searchJob = searchScope.launch {
                    delay(300)
                    onSearch(query)
                }
            }
        })
    }

    fun triggerSearch(query: String) {
        searchJob?.cancel()
        searchJob = searchScope.launch { 
            onSearch(query) 
        }
    }
}