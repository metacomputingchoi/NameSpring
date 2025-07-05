// utils/analysis/HanjaAnalyzer.kt
package com.ssc.namespring.utils.analysis

import com.ssc.namespring.model.data.source.HanjaInfo
import com.ssc.namespring.utils.data.json.JsonDataRepository

internal class HanjaAnalyzer(private val repository: JsonDataRepository) {

    fun getHanjaMeaning(hanja: String): HanjaInfo? {
        return HanjaInfo(
            origin = repository.hanjaMeanings.hanjaOrigins[hanja],
            components = repository.hanjaMeanings.hanjaComponents[hanja],
            relatedCharacters = repository.hanjaMeanings.hanjaRelatedCharacters[hanja]
        )
    }

    fun hasPositiveMeaning(meaning: String): Boolean {
        return repository.hanjaMeanings.positiveMeanings.any { positive ->
            meaning.contains(positive)
        }
    }

    fun isMeaningHarmony(meaning1: String, meaning2: String): Boolean {
        val pattern = "${meaning1}_${meaning2}"
        return repository.hanjaMeanings.meaningHarmonyPatterns[pattern] == true
    }

    fun getElementCharacteristic(element: String): String {
        return repository.elementCharacteristics.elementCharacteristics[element]
            ?: "알 수 없는 오행"
    }
}