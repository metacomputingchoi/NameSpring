// model/presentation/components/SearchDialogManager.kt
package com.ssc.namespring.model.presentation.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.adapter.SurnameSearchAdapter
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.domain.entity.SurnameData

class SearchDialogManager {

    fun showSurnameDialog(context: Context, onSurnameSelected: (SurnameInfo?) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_surname_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                val results = if (query.isNotEmpty()) {
                    SurnameData.searchSurnames(query)
                } else emptyList()
                adapter.submitList(results)
            }
        })

        dialog.show()
    }

    fun showHanjaSearchDialog(
        context: Context,
        position: Int,
        initialQuery: String,
        onHanjaSelected: (Int, String, String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hanja_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        if (initialQuery.isNotEmpty()) {
            etSearch.setText(initialQuery)
        }

        val adapter = HanjaSearchAdapter { result ->
            NameData.getCharInfo(result.tripleKey)?.let { info ->
                onHanjaSelected(position, result.korean, result.hanja)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(context)
            .setTitle("한자 선택")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        adapter.onItemSelected = { dialog.dismiss() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                val results = if (query.isNotEmpty()) {
                    NameData.searchHanja(query)
                } else emptyList()
                adapter.submitList(results)
            }
        })

        if (initialQuery.isNotEmpty()) {
            val results = NameData.searchHanja(initialQuery)
            adapter.submitList(results)
        }

        dialog.show()
    }
}