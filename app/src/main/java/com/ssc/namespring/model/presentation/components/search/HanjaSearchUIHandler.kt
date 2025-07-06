// model/presentation/components/search/HanjaSearchUIHandler.kt
package com.ssc.namespring.model.presentation.components.search

import android.annotation.SuppressLint
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter

internal class HanjaSearchUIHandler {

    @SuppressLint("SetTextI18n")
    fun setupUI(
        dialogView: View,
        hasKoreanConstraint: Boolean,
        isChosung: Boolean,
        initialKorean: String,
        adapter: HanjaSearchAdapter
    ) {
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val tvSearchHint = dialogView.findViewById<TextView>(R.id.tvSearchHint)
        val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)
        val chipGroupSearchMode = dialogView.findViewById<ChipGroup>(R.id.chipGroupSearchMode)

        recyclerView.layoutManager = LinearLayoutManager(dialogView.context)
        recyclerView.adapter = adapter

        chipGroupSearchMode.visibility = View.VISIBLE

        if (hasKoreanConstraint) {
            tvSearchHint.visibility = View.VISIBLE
            if (isChosung) {
                tvSearchHint.text = "초성 '$initialKorean'로 시작하는 한자 내에서 검색"
            } else {
                tvSearchHint.text = "'$initialKorean' 발음의 한자 내에서 검색"
            }
            tilSearch.hint = "결과 내 재검색"
        } else {
            tvSearchHint.visibility = View.GONE
        }
    }

    fun initializeSearchField(dialogView: View) {
        val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)

        tilSearch.isHintAnimationEnabled = true
        etSearch.setText(" ")
        etSearch.setText("")
    }
}