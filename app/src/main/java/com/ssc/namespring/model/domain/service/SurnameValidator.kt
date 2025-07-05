// model/domain/service/SurnameValidator.kt
package com.ssc.namespring.model.domain.service

import android.util.Log
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.source.SurnameStore

class SurnameValidator(private val store: SurnameStore) {
    companion object {
        private const val TAG = "SurnameValidator"
    }

    fun validate(): DataLoader.ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        Log.d(TAG, "=== 성씨 데이터 검증 시작 ===")

        validateBasicData(criticalErrors)
        validateCharTripleDict(warnings)
        validateCompoundSurnames(warnings)

        Log.d(TAG, "=== 성씨 데이터 검증 완료 ===")
        Log.d(TAG, "경고: ${warnings.size}개, 치명적 오류: ${criticalErrors.size}개")

        return DataLoader.ValidationResult(
            isValid = warnings.isEmpty() && criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    private fun validateBasicData(criticalErrors: MutableList<String>) {
        if (store.charTripleDict.isEmpty()) {
            criticalErrors.add("성씨 charTripleDict가 비어있음")
        }
        if (store.surnameMapping.isEmpty()) {
            criticalErrors.add("surnameMapping이 비어있음")
        }
    }

    private fun validateCharTripleDict(warnings: MutableList<String>) {
        var missingCount = 0
        store.surnameMapping.forEach { (korean, hanjaList) ->
            hanjaList.forEach { hanja ->
                val key = "$korean/$hanja"
                if (!store.charTripleDict.containsKey(key)) {
                    missingCount++
                    Log.v(TAG, "Missing in charTripleDict: $key")
                }
            }
        }

        if (missingCount > 0) {
            warnings.add("성씨 charTripleDict에서 $missingCount 개의 키 누락")
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