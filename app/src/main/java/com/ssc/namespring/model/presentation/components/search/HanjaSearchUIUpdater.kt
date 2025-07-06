// model/presentation/components/search/HanjaSearchUIUpdater.kt
package com.ssc.namespring.model.presentation.components.search

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.presentation.components.SearchDialogManager.SearchMode

internal class HanjaSearchUIUpdater(
    private val dialogView: View,
    private val hasKoreanConstraint: Boolean,
    private var baseResults: List<HanjaSearchResult>
) {

    private val tvResultCount = dialogView.findViewById<TextView>(R.id.tvResultCount)
    private val llResultInfo = dialogView.findViewById<LinearLayout>(R.id.llResultInfo)
    private val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)

    @SuppressLint("SetTextI18n")
    fun updateResults(results: List<HanjaSearchResult>) {
        llResultInfo?.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE

        if (hasKoreanConstraint) {
            tvResultCount?.text = "검색 결과: ${results.size}개 / 전체: ${baseResults.size}개"
        } else {
            tvResultCount?.text = "검색 결과: ${results.size}개"
        }
    }

    fun updateSearchHint(mode: SearchMode, hasKoreanConstraint: Boolean) {
        if (!hasKoreanConstraint) {
            tilSearch.hint = when (mode) {
                SearchMode.ALL -> "초성, 한글, 한자, 뜻, 획수 검색"
                SearchMode.SOUND -> "초성(ㅁ) 또는 한글(민) 검색"
                SearchMode.MEANING -> "뜻 검색 (예: 밝을, 지혜, ㄷㅎ)"
                SearchMode.HANJA -> "한자 또는 획수 검색 (예: 敏, 15)"
            }
        }
    }

    fun updateBaseResults(newBaseResults: List<HanjaSearchResult>) {
        baseResults = newBaseResults
    }
}