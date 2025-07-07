// ui/compare/filter/SearchFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.utils.search.KoreanSearchHelper

class SearchFilter(private var query: String) : NameFilter {
    private val searchHelper = KoreanSearchHelper()

    fun updateQuery(newQuery: String) {
        query = newQuery
    }

    override fun matches(favorite: FavoriteName): Boolean {
        if (query.isEmpty()) return true

        // 전체 이름으로 검색
        if (searchHelper.matches(favorite.fullNameKorean, query)) return true

        // 한자로도 검색
        if (favorite.fullNameHanja.contains(query)) return true

        return false
    }
}