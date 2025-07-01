// model/data/NameFilter.kt
package com.ssc.namespring.model.data

/**
 * 작명 필터 정보
 * UI에서 입력받은 조건을 NamingEngine 입력 형식으로 변환
 */
data class NameFilter(
    val positions: List<CharacterFilter>,
    val filterMode: FilterMode = FilterMode.DETAIL
) {
    fun toInputString(): String {
        return positions.joinToString("") { it.toFilterString() }
    }

    fun hasFilter(): Boolean = 
        positions.any { it.filterType != FilterType.EMPTY }

    fun getFilterDescription(): String {
        return when (filterMode) {
            FilterMode.SIMPLE -> "간편 모드 (필터 OFF)"
            FilterMode.DETAIL -> {
                val conditions = positions.mapIndexedNotNull { index, filter ->
                    when (filter.filterType) {
                        FilterType.INITIAL_SOUND -> "${index + 1}번째: 초성 '${filter.value}'"
                        FilterType.CHARACTER -> "${index + 1}번째: 글자 '${filter.value}'"
                        FilterType.HANJA -> "${index + 1}번째: 한자 '${filter.hanja}'"
                        FilterType.EMPTY -> null
                    }
                }
                if (conditions.isEmpty()) "조건 없음" else conditions.joinToString(", ")
            }
        }
    }
}

data class CharacterFilter(
    val position: Int,              // 위치 (0: 성씨, 1~: 이름)
    val filterType: FilterType,
    val value: String? = null,      // 필터 값 (한글 또는 초성)
    val hanja: String? = null       // 한자 (있는 경우)
) {
    fun toFilterString(): String = when (filterType) {
        FilterType.INITIAL_SOUND -> value ?: "_"  // 초성은 그대로
        FilterType.CHARACTER -> "[${value ?: "_"}/${hanja ?: "_"}]"
        FilterType.HANJA -> "[_/${hanja ?: "_"}]"
        FilterType.EMPTY -> "[_/_]"
    }

    fun isValid(): Boolean = when (filterType) {
        FilterType.INITIAL_SOUND -> value?.matches(Regex("[ㄱ-ㅎ]")) == true
        FilterType.CHARACTER -> value?.matches(Regex("[가-힣]")) == true
        FilterType.HANJA -> hanja?.matches(Regex("[\u4e00-\u9fff]")) == true
        FilterType.EMPTY -> true
    }
}

enum class FilterType {
    INITIAL_SOUND,  // 초성
    CHARACTER,      // 글자
    HANJA,         // 한자
    EMPTY          // 빈칸
}

enum class FilterMode {
    SIMPLE,    // 간편 모드 (필터 OFF)
    DETAIL     // 상세 모드
}