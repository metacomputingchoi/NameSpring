// model/common/Constants.kt
package com.ssc.namespring.model.common

object Constants {

    // 천간 오행 매핑
    val STEM_ELEMENTS = mapOf(
        "甲" to "木", "乙" to "木",
        "丙" to "火", "丁" to "火",
        "戊" to "土", "己" to "土",
        "庚" to "金", "辛" to "金",
        "壬" to "水", "癸" to "水"
    )

    // 지지 오행 매핑
    val BRANCH_ELEMENTS = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
        "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
        "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
    )

    // 시주 배열
    val SIJU = arrayOf(
        arrayOf("甲子", "丙子", "戊子", "庚子", "壬子"),
        arrayOf("乙丑", "丁丑", "己丑", "辛丑", "癸丑"),
        arrayOf("丙寅", "戊寅", "庚寅", "壬寅", "甲寅"),
        arrayOf("丁卯", "己卯", "辛卯", "癸卯", "乙卯"),
        arrayOf("戊辰", "庚辰", "壬辰", "甲辰", "丙辰"),
        arrayOf("己巳", "辛巳", "癸巳", "乙巳", "丁巳"),
        arrayOf("庚午", "壬午", "甲午", "丙午", "戊午"),
        arrayOf("辛未", "癸未", "乙未", "丁未", "己未"),
        arrayOf("壬申", "甲申", "丙申", "戊申", "庚申"),
        arrayOf("癸酉", "乙酉", "丁酉", "己酉", "辛酉"),
        arrayOf("甲戌", "丙戌", "戊戌", "庚戌", "壬戌"),
        arrayOf("乙亥", "丁亥", "己亥", "辛亥", "癸亥")
    )

    // 한글 자모
    val INITIALS = arrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    val MEDIALS = arrayOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    val FINALES = arrayOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

    // 획수 정의
    val STROKES = buildMap {
        // 초성 획수
        val initialStrokes = mapOf(
            'ㄱ' to 2, 'ㄲ' to 4, 'ㄴ' to 2, 'ㄷ' to 3, 'ㄸ' to 6,
            'ㄹ' to 5, 'ㅁ' to 4, 'ㅂ' to 4, 'ㅃ' to 8, 'ㅅ' to 2,
            'ㅆ' to 4, 'ㅇ' to 1, 'ㅈ' to 3, 'ㅉ' to 6, 'ㅊ' to 4,
            'ㅋ' to 3, 'ㅌ' to 4, 'ㅍ' to 4, 'ㅎ' to 3
        )

        // 중성 획수
        val medialStrokes = mapOf(
            'ㅏ' to 2, 'ㅐ' to 3, 'ㅑ' to 3, 'ㅒ' to 4, 'ㅓ' to 2,
            'ㅔ' to 3, 'ㅕ' to 3, 'ㅖ' to 4, 'ㅗ' to 2, 'ㅘ' to 4,
            'ㅙ' to 5, 'ㅚ' to 3, 'ㅛ' to 3, 'ㅜ' to 2, 'ㅝ' to 4,
            'ㅞ' to 5, 'ㅟ' to 3, 'ㅠ' to 3, 'ㅡ' to 1, 'ㅢ' to 2, 'ㅣ' to 1
        )

        // 종성 획수
        val finalStrokes = mapOf(
            "" to 0, "ㄱ" to 2, "ㄲ" to 4, "ㄳ" to 4, "ㄴ" to 2,
            "ㄵ" to 5, "ㄶ" to 5, "ㄷ" to 3, "ㄹ" to 5, "ㄺ" to 7,
            "ㄻ" to 9, "ㄼ" to 9, "ㄽ" to 7, "ㄾ" to 9, "ㄿ" to 9,
            "ㅀ" to 8, "ㅁ" to 4, "ㅂ" to 4, "ㅄ" to 6, "ㅅ" to 2,
            "ㅆ" to 4, "ㅇ" to 1, "ㅈ" to 3, "ㅊ" to 4, "ㅋ" to 3,
            "ㅌ" to 4, "ㅍ" to 4, "ㅎ" to 3
        )

        // 통합
        putAll(initialStrokes.mapKeys { it.key.toString() })
        putAll(medialStrokes.mapKeys { it.key.toString() })
        putAll(finalStrokes)
    }

    // 길한 획수
    val GOOD_LUCK = setOf(
        1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25,
        29, 31, 32, 33, 35, 37, 38, 39, 41, 45, 47, 48, 52, 57,
        61, 63, 65, 67, 68, 81
    )

    // 초성 오행 매핑
    val INITIAL_ELEMENTS = mapOf(
        'ㄱ' to "木", 'ㅋ' to "木",
        'ㄴ' to "火", 'ㄷ' to "火", 'ㄹ' to "火", 'ㅌ' to "火",
        'ㅇ' to "土", 'ㅎ' to "土",
        'ㅅ' to "金", 'ㅈ' to "金", 'ㅊ' to "金",
        'ㅁ' to "水", 'ㅂ' to "水", 'ㅍ' to "水"
    )

    // 중성 음양 분류
    val YIN_MEDIALS = setOf('ㅓ', 'ㅕ', 'ㅔ', 'ㅖ', 'ㅜ', 'ㅠ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅢ', 'ㅡ')
    val YANG_MEDIALS = setOf('ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅣ')

    // 오행 순서 (상생/상극 계산용)
    val ELEMENTS_ORDER = listOf("木", "火", "土", "金", "水")

    // 한글 유니코드 상수
    const val HANGUL_BASE = 0xAC00
    const val INITIAL_COUNT = 588
    const val MEDIAL_COUNT = 28
    const val MEDIALS_PER_INITIAL = 21

    // 시간 구분 상수
    object TimeSlot {
        const val SLOT_23_30 = "23:30:00"
        const val SLOT_01_30 = "01:30:00"
        const val SLOT_03_30 = "03:30:00"
        const val SLOT_05_30 = "05:30:00"
        const val SLOT_07_30 = "07:30:00"
        const val SLOT_09_30 = "09:30:00"
        const val SLOT_11_30 = "11:30:00"
        const val SLOT_13_30 = "13:30:00"
        const val SLOT_15_30 = "15:30:00"
        const val SLOT_17_30 = "17:30:00"
        const val SLOT_19_30 = "19:30:00"
        const val SLOT_21_30 = "21:30:00"
    }

    // 날짜 관련 상수
    object DateConstants {
        const val FEBRUARY = 2
        const val APRIL = 4
        const val JUNE = 6
        const val SEPTEMBER = 9
        const val NOVEMBER = 11
        const val DECEMBER = 12

        const val DAYS_IN_FEBRUARY = 28
        const val DAYS_IN_FEBRUARY_LEAP = 29
        const val DAYS_IN_SHORT_MONTH = 30
        const val DAYS_IN_LONG_MONTH = 31

        const val LEAP_YEAR_DIVISOR = 4
        const val CENTURY_DIVISOR = 100
        const val LEAP_CENTURY_DIVISOR = 400
    }

    // 야자시 관련
    const val YAJASI_HOUR = 23
    const val YAJASI_MINUTE = 30

    // 오행 상생/상극 관계
    object ElementRelations {
        const val GENERATING_FORWARD = 1   // 상생 정방향
        const val GENERATING_BACKWARD = 4  // 상생 역방향
        const val CONFLICTING_FORWARD = 2  // 상극 정방향
        const val CONFLICTING_BACKWARD = 3 // 상극 역방향

        const val ELEMENT_COUNT = 5
    }

    // 사격 계산 상수
    const val JUNG_MODULO = 81
    const val STROKE_MODULO = 10
    const val YIN_YANG_MODULO = 2

    // 최소 점수 기준
    object MinScores {
        const val COMPLEX_SURNAME_SINGLE_NAME = 2
        const val SINGLE_SURNAME_SINGLE_NAME = 3
        const val DEFAULT = 4
    }

    // 이름 길이 제약
    const val MAX_EMPTY_SLOTS = 2

    // 획수 범위
    const val MIN_STROKE = 1
    const val MAX_STROKE = 27

    // 한글 범위
    const val HANGUL_START = '가'
    const val HANGUL_END = '힣'

    // 성명 길이 조합
    object NameLengthCombinations {
        val SINGLE_SINGLE = 1 to 1      // 성 1자, 이름 1자
        val SINGLE_DOUBLE = 1 to 2      // 성 1자, 이름 2자
        val SINGLE_TRIPLE = 1 to 3      // 성 1자, 이름 3자
        val SINGLE_QUAD = 1 to 4        // 성 1자, 이름 4자
        val DOUBLE_SINGLE = 2 to 1      // 성 2자, 이름 1자
        val DOUBLE_DOUBLE = 2 to 2      // 성 2자, 이름 2자
        val DOUBLE_TRIPLE = 2 to 3      // 성 2자, 이름 3자
        val DOUBLE_QUAD = 2 to 4        // 성 2자, 이름 4자
    }

    // 음양 균형 체크 관련
    object YinYangBalance {
        const val MIN_VARIETY = 2       // 최소 음양 다양성
        const val MAX_CONSECUTIVE_SINGLE = 1
        const val MAX_CONSECUTIVE_DOUBLE = 2
        const val MAX_CONSECUTIVE_TRIPLE = 3
    }

    // 자원오행 체크 관련
    object JawonCheck {
        object DoubleChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_MULTIPLE_SIZE = 2
            const val ONE_SINGLE_SIZE = 1
            const val ONE_MULTIPLE_SIZE = 2
            const val EXPECTED_DIFFERENT_COUNT = 2  // 두 글자는 서로 달라야 함
        }

        object TripleChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_DOUBLE_SIZE = 2
            const val ZERO_MULTIPLE_SIZE = 3
            const val TRIPLE_SUM = 3
            const val MIN_COUNT_PER_ELEMENT = 1
            const val EXPECTED_UNIQUE_COUNT = 3
        }

        object QuadChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_DOUBLE_SIZE = 2
            const val ZERO_TRIPLE_SIZE = 3
            const val ZERO_MULTIPLE_SIZE = 4
            const val PAIR_COUNT = 2
            const val SINGLE_COUNT = 1
            const val EXPECTED_PAIRS_FOR_DOUBLE = 2  // 2개 원소일 때 각각 2개씩
            const val EXPECTED_SINGLE_COUNT = 2      // 3개 원소일 때 1개짜리 2개
            const val EXPECTED_UNIQUE_COUNT = 4
        }
    }

    // 오행 조화 점수
    object HarmonyScores {
        const val CONFLICTING_FORWARD_DIFF = 4
        const val CONFLICTING_BACKWARD_DIFF = -6
        const val GENERATING_FORWARD_DIFF = 2
        const val GENERATING_BACKWARD_DIFF = -8
    }

    // 천간 그룹
    object StemGroups {
        val WOOD_STEMS = setOf('甲', '乙')
        val FIRE_STEMS = setOf('丙', '丁')
        val EARTH_STEMS = setOf('戊', '己')
        val METAL_STEMS = setOf('庚', '辛')
        val WATER_STEMS = setOf('壬', '癸')
    }

    // 야자시 관련
    const val YAJASI_DAY_INCREMENT = 1

    // 사격 계산
    object FourPillarAnalysis {
        const val FOUR_TYPES_COUNT = 4
        const val START_INDEX = 1
        const val PREVIOUS_INDEX_OFFSET = 1
        const val MIN_HARMONY_SCORE = 0
        const val MIN_REQUIRED_SCORE = 1
    }

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

    // 로그 태그
    const val LOG_TAG = "NamingSystem"

    // 에러 메시지
    object ErrorMessages {
        const val INVALID_INPUT_FORMAT = "올바른 입력 형식이 아닙니다. 예: [김/金][_/_][ㅅ/_]"
        const val INVALID_SURNAME = "유효한 성을 찾을 수 없습니다."
        const val DATE_NOT_FOUND = "날짜 데이터를 찾을 수 없습니다."
        const val INVALID_HANGUL = "잘못된 한글 입력: "
        const val NAME_LENGTH_CONSTRAINT = "이름 길이 제약을 만족하지 않습니다: 성 "
    }
}