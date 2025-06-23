// model/application/service/template/NameAnalysisTemplate.kt
package com.ssc.namespring.model.application.service.template

import com.ssc.namespring.model.domain.name.value.NameAnalysisRequest
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance

abstract class NameAnalysisTemplate {

    fun analyzeNames(request: NameAnalysisRequest): List<Name> {
        val saju = request.saju ?: calculateSaju(request)
        val elementBalance = request.elementBalance ?: calculateElementBalance(saju)
        val combinations = generateCombinations(request)
        val filteredNames = filterNames(combinations, request, saju, elementBalance)
        return rankNames(filteredNames)
    }

    protected abstract fun calculateSaju(request: NameAnalysisRequest): Saju

    protected abstract fun calculateElementBalance(saju: Saju): ElementBalance

    protected abstract fun generateCombinations(request: NameAnalysisRequest): List<Map<String, Any>>

    protected abstract fun filterNames(
        combinations: List<Map<String, Any>>,
        request: NameAnalysisRequest,
        saju: Saju,
        elementBalance: ElementBalance
    ): List<Name>

    protected open fun rankNames(names: List<Name>): List<Name> {
        // 기본 구현: 정렬하지 않고 그대로 반환
        return names
    }
}