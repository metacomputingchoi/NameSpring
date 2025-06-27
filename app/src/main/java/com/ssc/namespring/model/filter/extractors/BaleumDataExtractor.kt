// model/filter/extractors/BaleumDataExtractor.kt
package com.ssc.namespring.model.filter.extractors

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName

class BaleumDataExtractor(
    private val getBaleumOhaeng: (Char) -> String?,
    private val getBaleumEumyang: (Char) -> Int?
) {

    fun extract(name: GeneratedName, context: FilterContext): BaleumData {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }
        val nameBaleumOhaeng = name.combinedPronounciation.mapNotNull { getBaleumOhaeng(it) }
        val nameBaleumEumyang = name.combinedPronounciation.mapNotNull { getBaleumEumyang(it) }

        return BaleumData(
            surBaleumOhaeng = surBaleumOhaeng,
            surBaleumEumyang = surBaleumEumyang,
            nameBaleumOhaeng = nameBaleumOhaeng,
            nameBaleumEumyang = nameBaleumEumyang,
            combinedBaleumOhaeng = (surBaleumOhaeng + nameBaleumOhaeng).joinToString(""),
            combinedEumyang = (surBaleumEumyang + nameBaleumEumyang).joinToString("") { it.toString() }
        )
    }
}

data class BaleumData(
    val surBaleumOhaeng: List<String>,
    val surBaleumEumyang: List<Int>,
    val nameBaleumOhaeng: List<String>,
    val nameBaleumEumyang: List<Int>,
    val combinedBaleumOhaeng: String,
    val combinedEumyang: String
)