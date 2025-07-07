// ui/compare/filter/SurnameFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName

class SurnameFilter(private val surname: String) : NameFilter {

    override fun matches(favorite: FavoriteName): Boolean {
        return favorite.surnameKorean == surname || favorite.surnameHanja == surname
    }
}