// ui/compare/filter/FilterCoordinator.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import android.view.View
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.filter.FilterConfig
import com.ssc.namespring.ui.compare.FilterType  // 변경됨

class FilterCoordinator(private val context: Context) {
    // 나머지 코드는 동일
    private val filterHandlers = mutableListOf<IFilterHandler>()

    fun initialize(minScore: Int, maxScore: Int) {
        filterHandlers.clear()

        // 점수 범위 필터
        filterHandlers.add(ScoreFilterHandler(minScore, maxScore))

        // 날짜 범위 필터
        filterHandlers.add(DateFilterHandler(context))

        // 성씨 필터
        val surnames = listOf(
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"
        ).map { it to it }

        filterHandlers.add(ChipFilterHandler(
            context, FilterType.SURNAME,
            R.id.cbSurname, R.id.chipGroupSurname, surnames
        ))

        // 오행 필터
        val elements = listOf(
            "목(木)" to "木", "화(火)" to "火", "토(土)" to "土",
            "금(金)" to "金", "수(水)" to "水"
        )

        filterHandlers.add(ChipFilterHandler(
            context, FilterType.ELEMENT,
            R.id.cbElement, R.id.chipGroupElement, elements
        ))

        // 한자 필터
        filterHandlers.add(TextInputFilterHandler(
            context, FilterType.HANJA_CONTAINS,
            R.id.cbHanja, R.id.containerHanja,
            R.id.etHanja, R.id.btnAddHanja,
            R.id.chipGroupAddedHanja,
            "이미 추가된 한자입니다"
        ))

        // 의미 필터
        filterHandlers.add(TextInputFilterHandler(
            context, FilterType.MEANING,
            R.id.cbMeaning, R.id.containerMeaning,
            R.id.etMeaning, R.id.btnAddMeaning,
            R.id.chipGroupAddedMeaning,
            "이미 추가된 키워드입니다"
        ))
    }

    fun setupViews(view: View) {
        filterHandlers.forEach { it.setupView(view) }
    }

    fun collectActiveFilters(): List<Pair<FilterType, Any>> {
        return filterHandlers
            .flatMap { it.collectFilters() }
            .map { it.filterType to it.value }
    }

    fun resetAll() {
        filterHandlers.forEach { it.reset() }
    }
}