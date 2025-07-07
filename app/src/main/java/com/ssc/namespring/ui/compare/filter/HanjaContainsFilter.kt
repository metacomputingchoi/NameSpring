// ui/compare/filter/HanjaContainsFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName

class HanjaContainsFilter(private val hanja: String) : NameFilter {

    override fun matches(favorite: FavoriteName): Boolean {
        return favorite.nameHanja.contains(hanja) || favorite.surnameHanja.contains(hanja)
    }
}