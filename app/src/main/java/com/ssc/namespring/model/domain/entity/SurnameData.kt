// model/domain/entity/SurnameData.kt
package com.ssc.namespring.model.domain.entity

import android.content.Context
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.source.SurnameLoader
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.search.SurnameSearchService
import com.ssc.namespring.model.domain.service.validation.SurnameValidator

object SurnameData {
    private val store = SurnameStore()
    private val loader = SurnameLoader(store)
    private val validator = SurnameValidator(store)
    private val searchService = SurnameSearchService(store)

    fun init(context: Context) {
        loader.loadData(context)
    }

    fun validateData(): ValidationResult {
        return validator.validate()
    }

    fun getAllSurnames(): List<SurnameSearchResult> {
        return searchService.getAllSurnames()
    }

    fun searchSurnames(query: String): List<SurnameSearchResult> {
        return searchService.search(query)
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return searchService.getSurnameInfo(korean, hanja)
    }
}