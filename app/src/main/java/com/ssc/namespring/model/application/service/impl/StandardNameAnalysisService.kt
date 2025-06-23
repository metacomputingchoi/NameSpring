// model/application/service/impl/StandardNameAnalysisService.kt
package com.ssc.namespring.model.application.service.impl

import com.ssc.namespring.model.application.service.SajuService
import com.ssc.namespring.model.application.service.NameCombinationService
import com.ssc.namespring.model.application.service.NameFilteringService
import com.ssc.namespring.model.domain.name.value.NameAnalysisRequest
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.infrastructure.repository.NameRepository

class StandardNameAnalysisService(
    private val sajuService: SajuService,
    private val nameCombinationService: NameCombinationService,
    private val nameFilteringService: NameFilteringService,
    private val nameRepository: NameRepository
) {
    fun analyzeNames(request: NameAnalysisRequest): List<Name> {
        // 사주 계산
        val saju = request.saju ?: sajuService.calculateSaju(
            request.birthDateTime.year,
            request.birthDateTime.month,
            request.birthDateTime.day,
            request.birthDateTime.hour,
            request.birthDateTime.minute
        )

        // 오행 균형 계산
        val elementBalance = request.elementBalance ?: sajuService.calculateElementBalance(saju)

        // 성씨 한글 확인
        val finalSurHangul = request.surHangul
            ?: nameRepository.getHangulSurnameFromHanja(request.surHanja)

        // 이름 조합 생성
        val combinations = nameCombinationService.generateCombinations(finalSurHangul, request.surHanja)

        // 0개와 1개 오행 추출
        val zeroElements = elementBalance.toMap()
            .filter { it.value == 0 }
            .map { it.key }

        val oneElements = elementBalance.toMap()
            .filter { it.value == 1 }
            .map { it.key }

        // 필터링 및 결과 반환
        return nameFilteringService.filterNames(
            combinations,
            finalSurHangul,
            request.surHanja,
            request.name1Hangul,
            request.name1Hanja,
            request.name2Hangul,
            request.name2Hanja,
            saju,
            elementBalance,
            zeroElements,
            oneElements,
            request.birthDateTime
        )
    }
}