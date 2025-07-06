// model/domain/service/search/SurnameInfoBuilder.kt
package com.ssc.namespring.model.domain.service.search

import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.data.source.SurnameStore

class SurnameInfoBuilder(private val store: SurnameStore) {

    fun build(korean: String, hanja: String): SurnameInfo? {
        val key = "$korean/$hanja"

        if (korean.length > 1) {
            return buildCompoundSurnameInfo(key, korean, hanja)
        }

        return store.charTripleDict[key]?.let { info ->
            SurnameInfo(
                korean = korean,
                hanja = hanja,
                meaning = info.integratedInfo.nameMeaning,
                strokes = info.hanjaInfo.strokes,
                ohaeng = info.hanjaInfo.ohaeng,
                eumyang = info.hanjaInfo.eumyang
            )
        }
    }

    private fun buildCompoundSurnameInfo(key: String, korean: String, hanja: String): SurnameInfo? {
        return store.surnameHanjaMapping[key]?.let { parts ->
            val meanings = collectMeanings(parts)
            var totalStrokes = 0
            var firstOhaeng: String? = null
            var firstEumyang = 0

            parts.forEach { partKey ->
                store.charTripleDict[partKey]?.let { info ->
                    totalStrokes += info.hanjaInfo.strokes
                    if (firstOhaeng == null) {
                        firstOhaeng = info.hanjaInfo.ohaeng
                        firstEumyang = info.hanjaInfo.eumyang
                    }
                }
            }

            SurnameInfo(
                korean = korean,
                hanja = hanja,
                meaning = meanings.joinToString("; ").ifEmpty { null }, // 세미콜론으로 구분
                strokes = totalStrokes,
                ohaeng = firstOhaeng,
                eumyang = firstEumyang
            )
        }
    }

    private fun collectMeanings(parts: List<String>): List<String> {
        val meanings = mutableListOf<String>()
        parts.forEach { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.let { info ->
                val meaning = info.nameMeaning ?: ""
                val korean = partKey.split("/")[0]
                val hanja = partKey.split("/")[1]
                if (meaning.isNotEmpty()) {
                    meanings.add("$korean($hanja): $meaning")
                } else {
                    meanings.add("$korean($hanja)")
                }
            }
        }
        return meanings
    }
}
