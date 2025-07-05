// model/domain/entity/SurnameData.kt
package com.ssc.namespring.model.domain.entity

import android.content.Context
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.source.SurnameLoader
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.SurnameSearcher
import com.ssc.namespring.model.domain.service.SurnameValidator

object SurnameData {
    private val store = SurnameStore()
    private val loader = SurnameLoader(store)
    private val validator = SurnameValidator(store)
    private val searcher = SurnameSearcher(store)

    fun init(context: Context) {
        loader.loadData(context)
    }

    fun validateData(): DataLoader.ValidationResult {
        return validator.validate()
    }

    fun searchSurnames(query: String): List<SurnameSearchResult> {
        return searcher.search(query)
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return searcher.getSurnameInfo(korean, hanja)
    }
}