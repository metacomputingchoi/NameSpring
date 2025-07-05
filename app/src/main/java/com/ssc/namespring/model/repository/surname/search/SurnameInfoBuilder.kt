// model/repository/surname/search/SurnameInfoBuilder.kt
package com.ssc.namespring.model.repository.surname.search

import com.ssc.namespring.model.data.SurnameInfo
import com.ssc.namespring.model.repository.surname.SurnameStore

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
                meaning = meanings.joinToString(" ").ifEmpty { null },
                strokes = totalStrokes,
                ohaeng = firstOhaeng,
                eumyang = firstEumyang
            )
        }
    }

    private fun collectMeanings(parts: List<String>): List<String> {
        val meanings = mutableListOf<String>()
        parts.forEach { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.nameMeaning?.let {
                meanings.add(it)
            }
        }
        return meanings
    }
}