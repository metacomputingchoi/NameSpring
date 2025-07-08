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
    internal var baseResults: List<HanjaSearchResult>
) {

    private val tvResultCount = dialogView.findViewById<TextView>(R.id.tvResultCount)
    private val llResultInfo = dialogView.findViewById<LinearLayout>(R.id.llResultInfo)
    private val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)

    @SuppressLint("SetTextI18n")
    fun updateResults(results: List<HanjaSearchResult>) {
        llResultInfo?.visibility = View.VISIBLE

        tvResultCount?.text = when {
            // 검색어가 없어서 전체를 보여주는 경우
            results.size == baseResults.size && baseResults.isNotEmpty() -> {
                "전체 ${results.size}개의 한자"
            }

            // 검색 결과가 있는 경우
            results.isNotEmpty() -> {
                if (hasKoreanConstraint) {
                    "${results.size}개 찾음"
                } else {
                    "${results.size}개의 검색 결과"
                }
            }

            // 검색 결과가 없는 경우
            else -> {
                "검색 결과가 없습니다"
            }
        }

        // 검색 결과가 없을 때 추가 안내
        if (results.isEmpty()) {
            tvResultCount?.append("\n다른 검색어를 시도해보세요")
        }
    }

    fun updateSearchHint(mode: SearchMode, hasKoreanConstraint: Boolean) {
        tilSearch.hint = when {
            hasKoreanConstraint -> {
                "추가 검색 (뜻, 한자 등)"
            }
            else -> {
                when (mode) {
                    SearchMode.ALL -> "검색어 입력 (초성, 한글, 한자, 뜻)"
                    SearchMode.SOUND -> "음으로 검색 (ㅁ, 민 등)"
                    SearchMode.MEANING -> "뜻으로 검색 (밝을, 지혜 등)"
                    SearchMode.HANJA -> "한자로 검색"
                }
            }
        }
    }

    fun updateBaseResults(newBaseResults: List<HanjaSearchResult>) {
        baseResults = newBaseResults
    }
}