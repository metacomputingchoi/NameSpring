// utils/analysis/HanjaAnalyzer.kt
package com.ssc.namespring.utils.analysis

import com.ssc.namespring.model.data.source.HanjaInfoMeaning
import com.ssc.namespring.utils.data.json.JsonDataRepository

internal class HanjaAnalyzer(private val repository: JsonDataRepository) {

    fun getHanjaMeaning(hanja: String): String {
        // hanjaMeanings는 lateinit이므로 직접 접근
        val originInfo = repository.hanjaMeanings.hanjaOrigins[hanja]
        return originInfo?.meaning ?: "의미를 찾을 수 없습니다"
    }

    fun getHanjaInfo(hanja: String): HanjaInfoMeaning? {
        val originInfo = repository.hanjaMeanings.hanjaOrigins[hanja]
        val componentInfo = repository.hanjaMeanings.hanjaComponents[hanja]
        val relatedChars = repository.hanjaMeanings.hanjaRelatedCharacters[hanja]

        return if (originInfo != null) {
            HanjaInfoMeaning(
                meaning = originInfo.meaning,
                origin = originInfo.origin,
                usage = originInfo.usage,
                components = componentInfo,
                relatedCharacters = relatedChars
            )
        } else {
            null
        }
    }

    fun hasPositiveMeaning(meaning: String): Boolean {
        return repository.hanjaMeanings.positiveMeanings.any { positive ->
            meaning.contains(positive)
        }
    }

    fun isMeaningHarmony(meaning1: String, meaning2: String): Boolean {
        val pattern = "${meaning1}_${meaning2}"
        // meaningHarmonyPatterns가 String 타입이므로 값이 존재하는지만 체크
        return repository.hanjaMeanings.meaningHarmonyPatterns.containsKey(pattern)
    }

    fun getElementCharacteristic(element: String): String {
        // elementCharacteristics는 lateinit이므로 직접 접근
        return repository.elementCharacteristics.elementCharacteristics[element]
            ?: "알 수 없는 오행"
    }
}