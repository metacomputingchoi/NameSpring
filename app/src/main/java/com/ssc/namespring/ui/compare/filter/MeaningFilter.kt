// ui/compare/filter/MeaningFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namingengine.data.GeneratedName

class MeaningFilter(private val keyword: String) : NameFilter {
    private val gson = Gson()

    override fun matches(favorite: FavoriteName): Boolean {
        return try {
            val generatedName = gson.fromJson(favorite.jsonData, GeneratedName::class.java)

            // 한자 의미에서 검색
            generatedName.hanjaDetails.any { hanjaInfo ->
                hanjaInfo.inmyongMeaning.contains(keyword)
            }
        } catch (e: Exception) {
            false
        }
    }
}