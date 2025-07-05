// model/domain/entity/NameData.kt
// model/repository/NameData.kt
package com.ssc.namespring.model.domain.entity

import android.content.Context
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.repository.NameDataRepository
import com.ssc.namespring.model.data.repository.NameDataRepositoryImpl
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.MappingStats

class NameData {
    companion object {
        private val repository: NameDataRepository = NameDataRepositoryImpl()

        @JvmStatic
        fun init(context: Context) = repository.init(context)

        @JvmStatic
        fun searchHanja(query: String): List<HanjaSearchResult> = repository.searchHanja(query)

        @JvmStatic
        fun getCharInfo(tripleKey: String): CharTripleInfo? = repository.getCharInfo(tripleKey)

        @JvmStatic
        fun getCharInfo(korean: String, hanja: String): CharTripleInfo? =
            repository.getCharInfo(korean, hanja)

        @JvmStatic
        fun validateData(): DataLoader.ValidationResult {
            val result = repository.validateData()
            return DataLoader.ValidationResult(
                isValid = result.isValid,
                warnings = result.warnings,
                criticalErrors = result.criticalErrors
            )
        }

        @JvmStatic
        fun isReady(): Boolean = repository.isReady()

        @JvmStatic
        fun getStats(): MappingStats? = repository.getStats()
    }
}