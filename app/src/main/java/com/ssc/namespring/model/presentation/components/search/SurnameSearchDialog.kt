// model/presentation/components/search/SurnameSearchDialog.kt
package com.ssc.namespring.model.presentation.components.search

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.adapter.SurnameSearchAdapter
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.SurnameData

internal class SurnameSearchDialog {

    fun show(context: Context, onSurnameSelected: (SurnameInfo?) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_surname_search, null)
        val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)
        val etSearch = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = SurnameSearchAdapter { result ->
            val surnameInfo = SurnameData.getSurnameInfo(result.korean, result.hanja)
            onSurnameSelected(surnameInfo)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(context)
            .setTitle("성씨 선택")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        adapter.onItemSelected = { dialog.dismiss() }

        setupSearch(etSearch, adapter)
        setupKeyboardAction(etSearch, adapter)

        dialog.show()
        loadInitialData(adapter)
        etSearch.requestFocus()
    }

    private fun setupSearch(etSearch: TextInputEditText, adapter: SurnameSearchAdapter) {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch(s?.toString() ?: "", adapter)
            }
        })
    }

    private fun setupKeyboardAction(etSearch: TextInputEditText, adapter: SurnameSearchAdapter) {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.text?.toString() ?: "", adapter)
                etSearch.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String, adapter: SurnameSearchAdapter) {
        try {
            val results = if (query.isNotEmpty()) {
                SurnameData.searchSurnames(query)
            } else {
                SurnameData.getAllSurnames()
            }
            adapter.submitList(results)
        } catch (e: Exception) {
            Log.e("SurnameSearchDialog", "성씨 검색 중 오류", e)
            adapter.submitList(emptyList())
        }
    }

    private fun loadInitialData(adapter: SurnameSearchAdapter) {
        try {
            val allSurnames = SurnameData.getAllSurnames()
            adapter.submitList(allSurnames)
        } catch (e: Exception) {
            Log.e("SurnameSearchDialog", "초기 성씨 목록 로드 실패", e)
        }
    }
}