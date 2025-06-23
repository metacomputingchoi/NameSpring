// model/application/service/filter/impl/HanjaSelectionFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name

class HanjaSelectionFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        // 한자 선택 관련 추가 검증 로직
        // 예: 특정 한자 조합 금지, 의미상 충돌 체크 등

        val hanja1 = name.hanja1Info.hanja
        val hanja2 = name.hanja2Info.hanja

        // 동일 한자 반복 체크
        if (hanja1 == hanja2) {
            return FilterResult(
                passed = false,
                reason = "same_hanja_repeated"
            )
        }

        // 부정적 의미 조합 체크
        val cautionRed1 = name.hanja1Info.cautionRed
        val cautionRed2 = name.hanja2Info.cautionRed

        if (!cautionRed1.isNullOrEmpty() || !cautionRed2.isNullOrEmpty()) {
            return FilterResult(
                passed = false,
                reason = "negative_meaning_combination",
                details = mapOf(
                    "caution1" to (cautionRed1 ?: ""),
                    "caution2" to (cautionRed2 ?: "")
                )
            )
        }

        return FilterResult(passed = true)
    }

    override fun getFilterName() = "hanja_selection_check"
}