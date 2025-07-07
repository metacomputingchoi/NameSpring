// ui/compare/filter/ElementFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namingengine.data.GeneratedName

class ElementFilter(private val element: String) : NameFilter {

    private val gson = Gson()

    override fun matches(favorite: FavoriteName): Boolean {
        return try {
            val generatedName = gson.fromJson(favorite.jsonData, GeneratedName::class.java)
            val ohaengInfo = generatedName.analysisInfo?.ohaengInfo
            ohaengInfo?.baleumOhaeng?.contains(element) == true ||
                    ohaengInfo?.jawonOhaeng?.contains(element) == true
        } catch (e: Exception) {
            false
        }
    }
}