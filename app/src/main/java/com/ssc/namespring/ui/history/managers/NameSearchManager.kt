// ui/history/managers/NameSearchManager.kt
package com.ssc.namespring.ui.history.managers

import com.ssc.namespring.ui.history.components.namelist.NameListSearchHelper
import com.ssc.namingengine.data.GeneratedName

class NameSearchManager {
    private val searchHelper = NameListSearchHelper()

    fun filter(names: List<GeneratedName>, query: String): List<GeneratedName> {
        return searchHelper.filterNames(names, query)
    }
}