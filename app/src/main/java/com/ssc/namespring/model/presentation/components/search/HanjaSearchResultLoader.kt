// model/presentation/components/search/HanjaSearchResultLoader.kt
package com.ssc.namespring.model.presentation.components.search

import android.util.Log
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class HanjaSearchResultLoader {

    suspend fun loadBaseResults(
        nameDataService: INameDataService,
        hasKoreanConstraint: Boolean,
        isChosung: Boolean,
        initialKorean: String
    ): List<HanjaSearchResult> {
        return withContext(Dispatchers.IO) {
            when {
                // 한글 제약이 없고 초기 검색어도 없으면 전체 한자
                !hasKoreanConstraint && initialKorean.isEmpty() -> {
                    nameDataService.getAllHanja()
                }
                // 초성 검색
                isChosung -> {
                    nameDataService.searchHanja(initialKorean)
                }
                // 특정 한글에 대한 한자만
                hasKoreanConstraint -> {
                    nameDataService.searchHanja(initialKorean)
                        .filter { it.korean == initialKorean }
                }
                // 그 외 전체 한자
                else -> {
                    nameDataService.getAllHanja()
                }
            }
        }.also {
            Log.d("HanjaSearchResultLoader", "베이스 결과: ${it.size}개")
        }
    }
}