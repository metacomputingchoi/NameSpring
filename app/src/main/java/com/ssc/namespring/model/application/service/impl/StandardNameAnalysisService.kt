// model/application/service/impl/StandardNameAnalysisService.kt
package com.ssc.namespring.model.application.service.impl

import com.ssc.namespring.model.application.factory.ServiceFactory
import com.ssc.namespring.model.application.service.filter.FilterChain
import com.ssc.namespring.model.application.service.template.NameAnalysisTemplate
import com.ssc.namespring.model.domain.name.value.NameAnalysisRequest
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.hanja.entity.Hanja
import com.ssc.namespring.model.common.util.HangulUtils

class StandardNameAnalysisService(
    private val serviceFactory: ServiceFactory
) : NameAnalysisTemplate() {

    override fun calculateSaju(request: NameAnalysisRequest): Saju {
        return serviceFactory.createSajuService().calculateSaju(
            request.birthDateTime.year,
            request.birthDateTime.month,
            request.birthDateTime.day,
            request.birthDateTime.hour,
            request.birthDateTime.minute
        )
    }

    override fun calculateElementBalance(saju: Saju): ElementBalance {
        return serviceFactory.createSajuService().calculateElementBalance(saju)
    }

    override fun generateCombinations(request: NameAnalysisRequest): List<Map<String, Any>> {
        val nameRepository = serviceFactory.nameRepository
        val finalSurHangul = request.surHangul
            ?: nameRepository.getHangulSurnameFromHanja(request.surHanja)

        return serviceFactory.createNameAnalysisService()
            .analyzeNameCombinations(finalSurHangul, request.surHanja)
    }

    override fun filterNames(
        combinations: List<Map<String, Any>>,
        request: NameAnalysisRequest,
        saju: Saju,
        elementBalance: ElementBalance
    ): List<Name> {
        val nameRepository = serviceFactory.nameRepository
        val hanjaRepository = serviceFactory.hanjaRepository
        val finalSurHangul = request.surHangul
            ?: nameRepository.getHangulSurnameFromHanja(request.surHanja)

        // 0개와 1개 오행 추출
        val zeroElements = elementBalance.toMap()
            .filter { it.value == 0 }
            .map { it.key }

        val oneElements = elementBalance.toMap()
            .filter { it.value == 1 }
            .map { it.key }

        val results = mutableListOf<Name>()
        val surHangulElement = HangulUtils.getHangulElement(finalSurHangul[0])
        val surHangulPm = HangulUtils.getHangulPn(finalSurHangul[0])

        // 필터 체인 생성
        val filterChain = FilterChain.createStandardChain(nameRepository)

        for (comb in combinations) {
            val analysisDetails = comb["analysis_details"] as Map<*, *>
            val stroke1 = comb["stroke1"] as Int
            val stroke2 = comb["stroke2"] as Int

            val hanja1List = findHanjaList(
                request.name1Hanja, request.name1Hangul, stroke1, hanjaRepository
            )
            val hanja2List = findHanjaList(
                request.name2Hanja, request.name2Hangul, stroke2, hanjaRepository
            )

            for (h1 in hanja1List) {
                for (h2 in hanja2List) {
                    if (h1.hanja.isEmpty() || h2.hanja.isEmpty() ||
                        h1.inmyeongYongEum.isNullOrEmpty() || h2.inmyeongYongEum.isNullOrEmpty()) {
                        continue
                    }

                    val name = Name(
                        surHangul = finalSurHangul,
                        surHanja = request.surHanja,
                        surHangulElement = surHangulElement,
                        surHangulPm = surHangulPm,
                        birthInfo = request.birthDateTime,
                        sajuInfo = saju,
                        dictElementsCount = elementBalance,
                        zeroElements = zeroElements,
                        oneElements = oneElements,
                        combinationAnalysis = serviceFactory.createNameAnalysisService()
                            .mapToCombinationAnalysis(analysisDetails),
                        hanja1Info = h1,
                        hanja2Info = h2,
                        filteringProcess = mutableListOf()
                    )

                    // 필터 체인 적용
                    val filterResult = filterChain.filter(name)
                    if (filterResult.passed) {
                        results.add(name)
                    }
                }
            }
        }

        return results
    }

    private fun findHanjaList(
        hanjaChar: String?,
        hangul: String?,
        stroke: Int,
        hanjaRepository: com.ssc.namespring.model.infrastructure.repository.HanjaRepository
    ): List<Hanja> {
        return when {
            hanjaChar != null -> hanjaRepository.findByHanja(hanjaChar)
            hangul != null -> hanjaRepository.findByStrokeAndPronunciation(stroke, hangul)
            else -> hanjaRepository.findByStroke(stroke)
        }
    }
}