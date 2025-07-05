// model/repository/NameDataRepository.kt
package com.ssc.namespring.model.repository

import android.content.Context

interface NameDataRepository {
    fun init(context: Context)
    fun searchHanja(query: String): List<HanjaSearchResult>
    fun getCharInfo(tripleKey: String): NameData.CharTripleInfo?
    fun getCharInfo(korean: String, hanja: String): NameData.CharTripleInfo?
    fun validateData(): ValidationResult
    fun isReady(): Boolean
    fun getStats(): NameData.MappingStats?
}