// model/domain/service/search/SurnameResultProcessor.kt
package com.ssc.namespring.model.domain.service.search

import com.ssc.namespring.model.domain.entity.SurnameSearchResult

/**
 * 성씨 검색 결과 처리기
 */
class SurnameResultProcessor {
    fun processResults(results: List<SurnameSearchResult>): List<SurnameSearchResult> {
        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy({ !it.isCompound }, { it.korean }))
    }
}