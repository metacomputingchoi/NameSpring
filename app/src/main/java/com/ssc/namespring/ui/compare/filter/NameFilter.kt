// ui/compare/filter/NameFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName

interface NameFilter {
    fun matches(favorite: FavoriteName): Boolean
}