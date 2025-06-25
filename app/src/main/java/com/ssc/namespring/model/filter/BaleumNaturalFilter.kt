// model/filter/BaleumNaturalFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName

class BaleumNaturalFilter(
    private val dictProvider: () -> Set<String>
) : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        return names.filter { name ->
            val fullName = name.surnameHangul + name.combinedPronounciation
            if (fullName.length > context.surHangul.length) {
                val namePart = fullName.substring(context.surHangul.length)
                namePart.length != 2 || namePart in dictProvider()
            } else {
                true
            }
        }
    }
}
