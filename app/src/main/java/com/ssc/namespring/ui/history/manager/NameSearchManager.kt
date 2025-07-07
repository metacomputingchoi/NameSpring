package com.ssc.namespring.ui.history.manager

import com.ssc.namespring.utils.search.NameSearchHelper
import com.ssc.namingengine.data.GeneratedName

class NameSearchManager(
    private val searchHelper: NameSearchHelper = NameSearchHelper()
) {

    fun filter(names: List<GeneratedName>, query: String): List<GeneratedName> {
        if (query.isEmpty()) {
            return names
        }

        return names.filter { name ->
            searchHelper.matches(name, query)
        }
    }
}