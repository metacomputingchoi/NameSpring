// model/domain/service/name/NameDataServiceImpl.kt
package com.ssc.namespring.model.domain.service.name

import android.content.Context
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.MappingStats
import com.ssc.namespring.model.data.repository.NameDataRepository

/**
 * INameDataService의 구현체
 * NameDataRepository를 통해 실제 데이터 접근을 수행
 */
class NameDataServiceImpl(
    private val repository: NameDataRepository
) : INameDataService {

    override fun init(context: Context) {
        repository.init(context)
    }

    override fun getAllHanja(): List<HanjaSearchResult> {
        return repository.getAllHanja()
    }

    override fun searchHanja(query: String): List<HanjaSearchResult> {
        return repository.searchHanja(query)
    }

    override fun getCharInfo(tripleKey: String): CharTripleInfo? {
        return repository.getCharInfo(tripleKey)
    }

    override fun getCharInfo(korean: String, hanja: String): CharTripleInfo? {
        return repository.getCharInfo(korean, hanja)
    }

    override fun validateData(): ValidationResult {
        return repository.validateData()
    }

    override fun searchHanjaByMeaning(query: String): List<HanjaSearchResult> {
        return repository.searchHanjaByMeaning(query)
    }

    override fun searchHanjaByHanja(query: String): List<HanjaSearchResult> {
        return repository.searchHanjaByHanja(query)
    }

    override fun isReady(): Boolean {
        return repository.isReady()
    }

    override fun getStats(): MappingStats? {
        return repository.getStats()
    }
}