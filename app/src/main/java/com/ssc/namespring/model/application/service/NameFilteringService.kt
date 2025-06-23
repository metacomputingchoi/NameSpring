// model/application/service/NameFilteringService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.domain.name.entity.*
import com.ssc.namespring.model.domain.saju.entity.BirthDateTime
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.infrastructure.repository.HanjaRepository
import com.ssc.namespring.model.infrastructure.repository.NameRepository
import com.ssc.namespring.model.common.util.HangulUtils
import com.ssc.namespring.model.application.service.filter.FilterChain

class NameFilteringService(
    private val hanjaRepository: HanjaRepository,
    private val nameRepository: NameRepository
) {
    fun filterNames(
        goodCombinations: List<NameCombination>,
        surHangul: String,
        surHanja: String,
        name1Hangul: String?,
        name1Hanja: String?,
        name2Hangul: String?,
        name2Hanja: String?,
        fourJu: Saju,
        dictElementsCount: ElementBalance,
        zeroElements: List<String>,
        oneElements: List<String>,
        birthInfo: BirthDateTime
    ): List<Name> {
        val results = mutableListOf<Name>()
        val surHangulElement = HangulUtils.getHangulElement(surHangul[0])
        val surHangulPm = HangulUtils.getHangulPn(surHangul[0])

        val filterChain = FilterChain.createStandardChain(nameRepository)

        for (combination in goodCombinations) {
            val hanja1List = findHanjaList(name1Hanja, name1Hangul, combination.stroke1)
            val hanja2List = findHanjaList(name2Hanja, name2Hangul, combination.stroke2)

            for (h1 in hanja1List) {
                for (h2 in hanja2List) {
                    if (h1.hanja.isEmpty() || h2.hanja.isEmpty() ||
                        h1.inmyeongYongEum.isNullOrEmpty() || h2.inmyeongYongEum.isNullOrEmpty()) {
                        continue
                    }

                    val name = Name(
                        surHangul = surHangul,
                        surHanja = surHanja,
                        surHangulElement = surHangulElement,
                        surHangulPm = surHangulPm,
                        birthInfo = birthInfo,
                        sajuInfo = fourJu,
                        dictElementsCount = dictElementsCount,
                        zeroElements = zeroElements,
                        oneElements = oneElements,
                        combinationAnalysis = combination,
                        hanja1Info = h1,
                        hanja2Info = h2,
                        filteringProcess = mutableListOf()
                    )

                    val filterResult = filterChain.filter(name)
                    if (filterResult.passed) {
                        results.add(name)
                    }
                }
            }
        }

        return results
    }

    private fun findHanjaList(hanjaChar: String?, hangul: String?, stroke: Int): List<com.ssc.namespring.model.domain.hanja.entity.Hanja> {
        return when {
            hanjaChar != null -> hanjaRepository.findByHanja(hanjaChar)
            hangul != null -> hanjaRepository.findByStrokeAndPronunciation(stroke, hangul)
            else -> hanjaRepository.findByStroke(stroke)
        }
    }
}