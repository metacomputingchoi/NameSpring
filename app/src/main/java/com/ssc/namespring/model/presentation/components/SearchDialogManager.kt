// model/presentation/components/SearchDialogManager.kt
package com.ssc.namespring.model.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.common.utils.MixedPatternUtils
import com.ssc.namespring.model.presentation.adapter.SurnameSearchAdapter
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.domain.entity.SurnameData

class SearchDialogManager {

    enum class SearchMode {
        ALL,      // 전체 검색 (음/뜻/한자 모두)
        SOUND,    // 음(音) 검색 (초성/한글)
        MEANING,  // 뜻 검색
        HANJA     // 한자 모양 검색
    }

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
                try {
                    val query = s?.toString() ?: ""
                    val results = if (query.isNotEmpty()) {
                        SurnameData.searchSurnames(query)
                    } else {
                        SurnameData.getAllSurnames()
                    }
                    adapter.submitList(results)
                } catch (e: Exception) {
                    Log.e("SearchDialog", "성씨 검색 중 오류", e)
                    adapter.submitList(emptyList())
                }
            }
        })

        dialog.show()

        try {
            val allSurnames = SurnameData.getAllSurnames()
            adapter.submitList(allSurnames)
        } catch (e: Exception) {
            Log.e("SearchDialog", "초기 성씨 목록 로드 실패", e)
        }
    }

    @SuppressLint("SetTextI18n")
    fun showHanjaSearchDialog(
        context: Context,
        position: Int,
        initialKorean: String,
        onHanjaSelected: (Int, String, String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hanja_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val tilSearch = dialogView.findViewById<TextInputLayout>(R.id.tilSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val tvSearchHint = dialogView.findViewById<TextView>(R.id.tvSearchHint)
        val chipGroupSearchMode = dialogView.findViewById<ChipGroup>(R.id.chipGroupSearchMode)
        val tvResultCount = dialogView.findViewById<TextView>(R.id.tvResultCount)
        val llResultInfo = dialogView.findViewById<LinearLayout>(R.id.llResultInfo)

        // 검색 모드 칩들
        val chipAll = dialogView.findViewById<Chip>(R.id.chipAll)
        val chipSound = dialogView.findViewById<Chip>(R.id.chipSound)
        val chipMeaning = dialogView.findViewById<Chip>(R.id.chipMeaning)
        val chipHanja = dialogView.findViewById<Chip>(R.id.chipHanja)

        val hasKoreanConstraint = initialKorean.trim().isNotEmpty()
        val isChosung = initialKorean.matches(Regex("^[ㄱ-ㅎ]+$"))
        var currentSearchMode = SearchMode.ALL

        // 베이스 결과 캐시
        var baseResults: List<HanjaSearchResult> = emptyList()

        // 검색 모드는 항상 표시
        chipGroupSearchMode.visibility = View.VISIBLE

        // 한글 제약 설명 설정
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
            updateSearchHint(tilSearch, currentSearchMode)
        }

        val adapter = HanjaSearchAdapter { result ->
            NameData.getCharInfo(result.tripleKey)?.let { info ->
                onHanjaSelected(position, result.korean, result.hanja)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(context)
            .setTitle(when {
                isChosung -> "초성 '$initialKorean' 한자 검색"
                hasKoreanConstraint -> "'$initialKorean' 한자 검색"
                else -> "한자 검색"
            })
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        adapter.onItemSelected = { dialog.dismiss() }

        // 베이스 결과 초기화
        fun loadBaseResults() {
            baseResults = if (hasKoreanConstraint) {
                if (isChosung) {
                    NameData.searchHanja(initialKorean)
                } else {
                    NameData.searchHanja(initialKorean)
                        .filter { it.korean == initialKorean }
                }
            } else {
                NameData.getAllHanja()
            }
            Log.d("SearchDialog", "베이스 결과: ${baseResults.size}개")
        }

        // 결과 업데이트 함수
        fun updateResults(results: List<HanjaSearchResult>) {
            adapter.submitList(results)
            llResultInfo?.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE

            if (hasKoreanConstraint) {
                tvResultCount?.text = "검색 결과: ${results.size}개 / 전체: ${baseResults.size}개"
            } else {
                tvResultCount?.text = "검색 결과: ${results.size}개"
            }
        }

        // 검색 수행 함수
        fun performSearch(query: String) {
            Log.d("SearchDialog", "검색 수행: query='$query', mode=$currentSearchMode")

            val results = if (query.isEmpty()) {
                baseResults
            } else {
                when (currentSearchMode) {
                    SearchMode.ALL -> searchAllInResults(baseResults, query)
                    SearchMode.SOUND -> searchSoundInResults(baseResults, query)
                    SearchMode.MEANING -> searchMeaningInResults(baseResults, query)
                    SearchMode.HANJA -> searchHanjaInResults(baseResults, query)
                }
            }

            Log.d("SearchDialog", "검색 결과: ${results.size}개")
            updateResults(results)
        }

        // 검색 모드 변경 리스너
        chipGroupSearchMode.setOnCheckedChangeListener { _, checkedId ->
            currentSearchMode = when (checkedId) {
                R.id.chipAll -> SearchMode.ALL
                R.id.chipSound -> SearchMode.SOUND
                R.id.chipMeaning -> SearchMode.MEANING
                R.id.chipHanja -> SearchMode.HANJA
                else -> SearchMode.ALL
            }

            if (!hasKoreanConstraint) {
                updateSearchHint(tilSearch, currentSearchMode)
            }

            val currentQuery = etSearch.text?.toString()?.trim() ?: ""
            performSearch(currentQuery)
        }

        // 검색어 입력 리스너
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                performSearch(query)
            }
        })

        dialog.show()

        // 초기 데이터 로드
        loadBaseResults()
        performSearch("")

        // 힌트를 위로 올리기
        tilSearch.isHintAnimationEnabled = true
        etSearch.setText(" ")
        etSearch.setText("")
    }

    // 전체 검색
    private fun searchAllInResults(baseResults: List<HanjaSearchResult>, query: String): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            val soundMatch = checkSound(result.korean, query)
            val meaningMatch = checkMeaning(result.meaning, query)
            val hanjaMatch = checkHanja(result.hanja, query)
            val strokeMatch = checkStroke(result.strokes, query)

            // 디버그용 로그
            if (meaningMatch) {
                Log.d("SearchDialog", "뜻 매치: ${result.korean}(${result.hanja}) - ${result.meaning}")
            }

            soundMatch || meaningMatch || hanjaMatch || strokeMatch
        }
    }

    // 음 검색
    private fun searchSoundInResults(baseResults: List<HanjaSearchResult>, query: String): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            checkSound(result.korean, query)
        }
    }

    // 뜻 검색
    private fun searchMeaningInResults(baseResults: List<HanjaSearchResult>, query: String): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            checkMeaning(result.meaning, query)
        }
    }

    // 한자 검색
    private fun searchHanjaInResults(baseResults: List<HanjaSearchResult>, query: String): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            checkHanja(result.hanja, query) || checkStroke(result.strokes, query)
        }
    }

    private fun checkSound(korean: String, query: String): Boolean {
        // 순수 초성 검색
        if (query.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            return MixedPatternUtils.matchChosungPattern(korean, query)
        }

        // 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            return MixedPatternUtils.matchMixedPattern(korean, query)
        }

        // 한글 검색
        if (query.matches(Regex("^[가-힣]+$"))) {
            if (korean == query) return true
            if (korean.contains(query)) return true
        }

        return false
    }

    private fun checkMeaning(meaning: String?, query: String): Boolean {
        if (meaning == null || meaning.isEmpty()) return false

        // 순수 초성 검색
        if (query.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            return MixedPatternUtils.matchChosungPattern(meaning, query)
        }

        // 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            return MixedPatternUtils.matchMixedPattern(meaning, query)
        }

        // 일반 텍스트 검색
        return meaning.contains(query, ignoreCase = true)
    }

    // 한자 체크
    private fun checkHanja(hanja: String, query: String): Boolean {
        return hanja.contains(query)
    }

    // 획수 체크
    private fun checkStroke(strokes: Int, query: String): Boolean {
        return query.toIntOrNull() == strokes
    }

    private fun updateSearchHint(tilSearch: TextInputLayout, mode: SearchMode) {
        tilSearch.hint = when (mode) {
            SearchMode.ALL -> "초성, 한글, 한자, 뜻, 획수 검색"
            SearchMode.SOUND -> "초성(ㅁ) 또는 한글(민) 검색"
            SearchMode.MEANING -> "뜻 검색 (예: 밝을, 지혜, ㄷㅎ)"  // 초성 예시 추가
            SearchMode.HANJA -> "한자 또는 획수 검색 (예: 敏, 15)"
        }
    }
}