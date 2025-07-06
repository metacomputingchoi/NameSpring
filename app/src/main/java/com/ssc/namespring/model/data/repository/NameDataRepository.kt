// model/data/repository/NameDataRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.MappingStats

interface NameDataRepository {
    fun init(context: Context)
    fun searchHanja(query: String): List<HanjaSearchResult>
    fun getAllHanja(): List<HanjaSearchResult> // 추가
    fun getCharInfo(tripleKey: String): CharTripleInfo?
    fun getCharInfo(korean: String, hanja: String): CharTripleInfo?
    fun validateData(): ValidationResult
    fun isReady(): Boolean
    fun getStats(): MappingStats?
    fun searchHanjaByMeaning(query: String): List<HanjaSearchResult>
    fun searchHanjaByHanja(query: String): List<HanjaSearchResult>
}