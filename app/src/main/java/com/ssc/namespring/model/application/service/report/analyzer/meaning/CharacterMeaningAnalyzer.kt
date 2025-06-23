// model/application/service/report/analyzer/meaning/CharacterMeaningAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.meaning

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.CharacterAnalysis
import com.ssc.namespring.model.domain.name.value.HanjaDetail
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class CharacterMeaningAnalyzer {
    private val hanjaMeaningsData = ReportDataHolder.hanjaMeaningsData
    private val strings = ReportDataHolder.characterMeaningStrings

    fun analyze(result: Name): CharacterAnalysis {
        val hanjaDetails = listOf(
            analyzeHanja(result.hanja1Info),
            analyzeHanja(result.hanja2Info)
        )

        return CharacterAnalysis(
            hanjaDetails = hanjaDetails,
            combinedMeaning = analyzeCombinedMeaning(result),
            symbolicInterpretation = analyzeSymbolic(result),
            culturalSignificance = analyzeCultural(result)
        )
    }

    private fun analyzeHanja(hanja: com.ssc.namespring.model.domain.hanja.entity.Hanja): HanjaDetail {
        return HanjaDetail(
            character = hanja.hanja,
            meaning = hanja.inmyeongYongDdeut ?: strings.defaultMeaning,
            origin = getHanjaOrigin(hanja.hanja),
            components = getHanjaComponents(hanja.hanja),
            relatedCharacters = getRelatedCharacters(hanja.hanja)
        )
    }

    private fun getHanjaOrigin(hanja: String): String {
        return hanjaMeaningsData.hanjaOrigins[hanja] ?: strings.originNotFound
    }

    private fun getHanjaComponents(hanja: String): List<String> {
        return hanjaMeaningsData.hanjaComponents[hanja] ?: emptyList()
    }

    private fun getRelatedCharacters(hanja: String): List<String> {
        return hanjaMeaningsData.hanjaRelatedCharacters[hanja] ?: emptyList()
    }

    private fun analyzeCombinedMeaning(result: Name): String {
        val meaning1 = result.hanja1Info.inmyeongYongDdeut ?: ""
        val meaning2 = result.hanja2Info.inmyeongYongDdeut ?: ""

        // 미리 정의된 조합 의미 확인
        for ((key, value) in hanjaMeaningsData.combinedMeanings) {
            val keywords = key.split(strings.patternDelimiter)
            if (keywords.all { keyword ->
                    meaning1.contains(keyword) || meaning2.contains(keyword)
                }) {
                return value
            }
        }

        return strings.combinedMeaningFormat
            .replace("{meaning1}", meaning1)
            .replace("{meaning2}", meaning2)
    }

    private fun analyzeSymbolic(result: Name): String {
        val elements = result.combinedElement ?: ""

        return hanjaMeaningsData.symbolicElements[elements]
            ?: strings.defaultSymbolic
    }

    private fun analyzeCultural(result: Name): String {
        return hanjaMeaningsData.culturalSignificance
    }
}