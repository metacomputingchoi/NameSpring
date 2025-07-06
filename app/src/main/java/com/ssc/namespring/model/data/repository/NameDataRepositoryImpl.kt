// model/data/repository/NameDataRepositoryImpl.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.data.source.NameDataLoader
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.service.search.NameDataSearchService
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.MappingStats

class NameDataRepositoryImpl : NameDataRepository {
    companion object {
        private const val TAG = "NameDataRepositoryImpl"
    }

    private val loader = NameDataLoader()
    private val searchService = NameDataSearchService()

    private var charTripleDict: Map<String, CharTripleInfo> = emptyMap()
    private var optimizedMapping: OptimizedMapping? = null
    private var isInitialized = false

    override fun init(context: Context) {
        try {
            optimizedMapping = loader.loadOptimizedMapping(context)
            charTripleDict = loader.loadCharTripleDict(context)

            isInitialized = optimizedMapping != null && charTripleDict.isNotEmpty()

            if (isInitialized && optimizedMapping != null) {
                searchService.initialize(optimizedMapping!!)
                Log.d(TAG, "NameData 초기화 완료")
            } else {
                throw IllegalStateException("데이터 로드 실패")
            }
        } catch (e: Exception) {
            Log.e(TAG, "초기화 실패", e)
            isInitialized = false
            throw e
        }
    }

    override fun searchHanja(query: String): List<HanjaSearchResult> {
        if (!isInitialized || optimizedMapping == null) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        return searchService.search(query)
    }

    override fun getAllHanja(): List<HanjaSearchResult> {
        if (!isInitialized || optimizedMapping == null) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        return searchService.getAllHanja()
    }

    override fun searchHanjaByMeaning(query: String): List<HanjaSearchResult> {
        if (!isInitialized || optimizedMapping == null) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        return searchService.searchByMeaning(query)
    }

    override fun searchHanjaByHanja(query: String): List<HanjaSearchResult> {
        if (!isInitialized || optimizedMapping == null) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        return searchService.searchByHanja(query)
    }

    override fun getCharInfo(tripleKey: String): CharTripleInfo? = charTripleDict[tripleKey]

    override fun getCharInfo(korean: String, hanja: String): CharTripleInfo? =
        charTripleDict["$korean/$hanja"]

    override fun validateData(): ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        if (!isInitialized) {
            criticalErrors.add("NameData가 초기화되지 않음")
        }

        if (optimizedMapping == null) {
            criticalErrors.add("최적화된 매핑이 로드되지 않음")
        }

        return ValidationResult(
            isValid = criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    override fun isReady(): Boolean = isInitialized

    override fun getStats(): MappingStats? = optimizedMapping?.stats
}