// model/domain/entity/NameData.kt
package com.ssc.namespring.model.domain.entity

import android.content.Context
import com.ssc.namespring.model.data.repository.NameDataRepository
import com.ssc.namespring.model.data.repository.NameDataRepositoryImpl
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.MappingStats

object NameData {
    private val repository: NameDataRepository = NameDataRepositoryImpl()

    @JvmStatic
    fun init(context: Context) = repository.init(context)

    @JvmStatic
    fun getAllHanja(): List<HanjaSearchResult> = repository.getAllHanja()

    @JvmStatic
    fun searchHanja(query: String): List<HanjaSearchResult> = repository.searchHanja(query)

    @JvmStatic
    fun getCharInfo(tripleKey: String): CharTripleInfo? = repository.getCharInfo(tripleKey)

    @JvmStatic
    fun getCharInfo(korean: String, hanja: String): CharTripleInfo? =
        repository.getCharInfo(korean, hanja)

    @JvmStatic
    fun validateData(): ValidationResult = repository.validateData()

    @JvmStatic
    fun searchHanjaByMeaning(query: String): List<HanjaSearchResult> =
        repository.searchHanjaByMeaning(query)

    @JvmStatic
    fun searchHanjaByHanja(query: String): List<HanjaSearchResult> =
        repository.searchHanjaByHanja(query)

    @JvmStatic
    fun isReady(): Boolean = repository.isReady()

    @JvmStatic
    fun getStats(): MappingStats? = repository.getStats()
}