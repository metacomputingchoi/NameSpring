// model/common/parsing/ParsingConstants.kt
package com.ssc.namespring.model.common.parsing

object ParsingConstants {
    // 제약 조건 타입
    object ConstraintTypes {
        const val EMPTY = "empty"
        const val INITIAL = "initial"
        const val COMPLETE = "complete"
    }

    // 입력 구분자
    const val INPUT_SEPARATOR = "_"
    const val NAME_PART_SEPARATOR = "/"
    const val NAME_PATTERN = "\\[([^/]+)/([^\\]]+)\\]"

    // JSON 키
    object JsonKeys {
        const val YEAR = "연"
        const val MONTH = "월"
        const val DAY = "일"
        const val YEAR_PILLAR = "연주"
        const val MONTH_PILLAR = "월주"
        const val DAY_PILLAR = "일주"
        const val INTEGRATED_INFO = "통합정보"
        const val HANJA = "한자"
        const val INMYONG_MEANING = "인명용 뜻"
        const val INMYONG_SOUND = "인명용 음"
        const val PRONUNCIATION_YINYANG = "발음음양"
        const val STROKE_YINYANG = "획수음양"
        const val PRONUNCIATION_ELEMENT = "발음오행"
        const val SOURCE_ELEMENT = "자원오행"
        const val ORIGINAL_STROKE = "원획수"
        const val DICTIONARY_STROKE = "옥편획수"
    }

    // 에러 메시지
    object ErrorMessages {
        const val INVALID_INPUT_FORMAT = "올바른 입력 형식이 아닙니다. 예: [김/金][_/_][ㅅ/_]"
        const val INVALID_SURNAME = "유효한 성을 찾을 수 없습니다."
        const val DATE_NOT_FOUND = "날짜 데이터를 찾을 수 없습니다."
        const val INVALID_HANGUL = "잘못된 한글 입력"
        const val NAME_LENGTH_CONSTRAINT = "이름 길이 제약을 만족하지 않습니다: 성 "
        const val INVALID_DATE = "유효하지 않은 날짜입니다"
        const val INVALID_TIME = "유효하지 않은 시간입니다"
        const val DATA_LOAD_FAILED = "데이터 로드 실패"
    }

    // 로그 태그
    const val LOG_TAG = "NamingSystem"
}
