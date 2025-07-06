// model/domain/service/validation/SurnameValidator.kt
package com.ssc.namespring.model.domain.service.validation

import android.util.Log
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.entity.ValidationResult

class SurnameValidator(private val store: SurnameStore) {
    companion object {
        private const val TAG = "SurnameValidator"
    }

    fun validate(): ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        Log.d(TAG, "=== 성씨 데이터 검증 시작 ===")

        validateBasicData(criticalErrors)
        validateCharTripleDict(warnings)
        validateCompoundSurnames(warnings)

        Log.d(TAG, "=== 성씨 데이터 검증 완료 ===")
        Log.d(TAG, "경고: ${warnings.size}개, 치명적 오류: ${criticalErrors.size}개")

        return ValidationResult(
            isValid = warnings.isEmpty() && criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    private fun validateBasicData(criticalErrors: MutableList<String>) {
        if (store.charTripleDict.isEmpty()) {
            criticalErrors.add("성씨 charTripleDict가 비어있음")
        }
    }

    private fun validateCharTripleDict(warnings: MutableList<String>) {
        var invalidCount = 0
        store.charTripleDict.forEach { (key, info) ->
            if (!key.contains("/") || key.count { it == '/' } != 1) {
                invalidCount++
                Log.v(TAG, "Invalid key format: $key")
            }
        }

        if (invalidCount > 0) {
            warnings.add("잘못된 형식의 키: $invalidCount 개")
        }
    }

    private fun validateCompoundSurnames(warnings: MutableList<String>) {
        var invalidCompoundCount = 0
        store.surnameHanjaMapping.forEach { (key, parts) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                parts.forEach { partKey ->
                    if (!store.charTripleDict.containsKey(partKey)) {
                        invalidCompoundCount++
                        Log.v(TAG, "Invalid compound surname part: $partKey in $key")
                    }
                }
            }
        }

        if (invalidCompoundCount > 0) {
            warnings.add("복성 매핑 경고: $invalidCompoundCount 개")
        }
    }
}
