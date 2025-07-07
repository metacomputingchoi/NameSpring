// utils/search/TaskSearchHelper.kt
package com.ssc.namespring.utils.search

import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namingengine.util.HangulUtils

class TaskSearchHelper {

    fun matches(task: Task, query: String): Boolean {
        if (query.isEmpty()) return true

        val lowerQuery = query.lowercase()

        // 작업명으로 검색
        val taskName = task.inputData["profileName"] as? String ?: ""
        if (matchesText(taskName, lowerQuery)) return true

        // 작업 유형으로 검색
        val taskTypeName = getTaskTypeName(task.type)
        if (matchesText(taskTypeName, lowerQuery)) return true

        // 작업 ID로 검색 (처음 8자리)
        if (task.id.take(8).lowercase().contains(lowerQuery)) return true

        return false
    }

    private fun matchesText(text: String, query: String): Boolean {
        val lowerText = text.lowercase()

        // 일반 포함 검색
        if (lowerText.contains(query)) return true

        // 초성 검색
        if (matchesChosung(text, query)) return true

        // 혼합 검색 (초성+완성형)
        if (matchesMixed(text, query)) return true

        return false
    }

    // 초성 검색 구현
    private fun matchesChosung(text: String, query: String): Boolean {
        // 텍스트에서 초성 추출
        val initials = HangulUtils.extractInitials(text)

        // 쿼리가 초성으로만 이루어져 있는지 확인
        val isChosungQuery = query.all { it in 'ㄱ'..'ㅎ' }

        if (isChosungQuery) {
            // 초성으로 검색
            return initials.contains(query)
        }

        return false
    }

    // 혼합 검색 구현 (초성+완성형 조합)
    private fun matchesMixed(text: String, query: String): Boolean {
        if (query.isEmpty() || text.isEmpty()) return false

        var textIndex = 0
        var queryIndex = 0

        while (textIndex < text.length && queryIndex < query.length) {
            val textChar = text[textIndex]
            val queryChar = query[queryIndex]

            when {
                // 쿼리 문자가 초성인 경우
                queryChar in 'ㄱ'..'ㅎ' -> {
                    val textInitial = HangulUtils.getInitialFromHangul(textChar)
                    if (textInitial == queryChar) {
                        queryIndex++
                        textIndex++
                    } else {
                        textIndex++
                    }
                }
                // 쿼리 문자가 일반 문자인 경우
                else -> {
                    if (textChar.lowercase() == queryChar.lowercase()) {
                        queryIndex++
                        textIndex++
                    } else {
                        textIndex++
                    }
                }
            }
        }

        // 모든 쿼리 문자가 매치되었는지 확인
        return queryIndex == query.length
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }
}