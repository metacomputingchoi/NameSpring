// model/data/DynamicInputData.kt
package com.ssc.namespring.model.data

/**
 * 동적 입력을 위한 데이터 클래스들
 * + 버튼으로 글자 수를 동적으로 조절하는 UI를 위한 데이터 구조
 */

data class DynamicNameInput(
    val surnameInputs: List<CharacterInput>,
    val givenNameInputs: List<CharacterInput>,
    val maxSurnameLength: Int = 2,
    val maxGivenNameLength: Int = 4
) {
    fun canAddSurnameChar(): Boolean =
        surnameInputs.size < maxSurnameLength

    fun canAddGivenNameChar(): Boolean =
        givenNameInputs.size < maxGivenNameLength

    fun addSurnameChar(): DynamicNameInput {
        if (!canAddSurnameChar()) return this
        return copy(
            surnameInputs = surnameInputs + CharacterInput(
                position = surnameInputs.size,
                isSurname = true
            )
        )
    }

    fun addGivenNameChar(): DynamicNameInput {
        if (!canAddGivenNameChar()) return this
        return copy(
            givenNameInputs = givenNameInputs + CharacterInput(
                position = givenNameInputs.size,
                isSurname = false
            )
        )
    }

    fun removeSurnameChar(): DynamicNameInput {
        if (surnameInputs.size <= 1) return this
        return copy(surnameInputs = surnameInputs.dropLast(1))
    }

    fun removeGivenNameChar(): DynamicNameInput {
        if (givenNameInputs.size <= 1) return this
        return copy(givenNameInputs = givenNameInputs.dropLast(1))
    }

    fun updateInput(position: Int, isSurname: Boolean, update: CharacterInput.() -> CharacterInput): DynamicNameInput {
        return if (isSurname) {
            copy(
                surnameInputs = surnameInputs.mapIndexed { index, input ->
                    if (index == position) input.update() else input
                }
            )
        } else {
            copy(
                givenNameInputs = givenNameInputs.mapIndexed { index, input ->
                    if (index == position) input.update() else input
                }
            )
        }
    }

    companion object {
        fun createDefault(): DynamicNameInput {
            return DynamicNameInput(
                surnameInputs = listOf(CharacterInput(0, true)),
                givenNameInputs = listOf(CharacterInput(0, false))
            )
        }
    }
}

data class CharacterInput(
    val position: Int,
    val isSurname: Boolean,
    val hangul: String = "",
    val hanja: String = "",
    val inputType: CharacterInputType = CharacterInputType.CHARACTER,
    val isValid: Boolean = true,
    val errorMessage: String? = null
) {
    fun validate(): CharacterInput {
        return when (inputType) {
            CharacterInputType.INITIAL_SOUND -> {
                val isValid = hangul.matches(Regex("[ㄱ-ㅎ]"))
                copy(
                    isValid = isValid,
                    errorMessage = if (!isValid) "초성만 입력 가능합니다" else null
                )
            }
            CharacterInputType.CHARACTER -> {
                val hangulValid = hangul.isEmpty() || hangul.matches(Regex("[가-힣]"))
                val hanjaValid = hanja.isEmpty() || hanja.matches(Regex("[\u4e00-\u9fff]"))
                val isValid = hangulValid && hanjaValid
                copy(
                    isValid = isValid,
                    errorMessage = when {
                        !hangulValid -> "한글만 입력 가능합니다"
                        !hanjaValid -> "한자만 입력 가능합니다"
                        else -> null
                    }
                )
            }
            CharacterInputType.HANJA_ONLY -> {
                val isValid = hanja.matches(Regex("[\u4e00-\u9fff]"))
                copy(
                    isValid = isValid,
                    errorMessage = if (!isValid) "한자를 입력하세요" else null
                )
            }
            CharacterInputType.EMPTY -> {
                copy(isValid = true, errorMessage = null)
            }
        }
    }

    fun toFilterString(): String {
        return when (inputType) {
            CharacterInputType.INITIAL_SOUND -> hangul
            CharacterInputType.CHARACTER -> "[$hangul/$hanja]"
            CharacterInputType.HANJA_ONLY -> "[_/$hanja]"
            CharacterInputType.EMPTY -> "[_/_]"
        }
    }
}

enum class CharacterInputType {
    INITIAL_SOUND,  // 초성만
    CHARACTER,      // 한글+한자
    HANJA_ONLY,    // 한자만
    EMPTY          // 빈칸 (자유)
}

/**
 * 동적 필터 입력 데이터
 * 작명 설정 화면에서 사용
 */
data class DynamicFilterInput(
    val surnameFilters: List<FilterInput>,
    val givenNameFilters: List<FilterInput>,
    val filterMode: FilterMode = FilterMode.DETAIL
) {
    fun toNameFilter(): NameFilter {
        // 성씨 최대 2자로 제한
        val maxSurnamePosition = 1

        val characterFilters = (surnameFilters + givenNameFilters).mapIndexed { index, filter ->
            // 실제 위치 계산 (성씨는 0-1, 이름은 2-5)
            val actualPosition = if (filter.isSurname) {
                filter.position
            } else {
                filter.position + maxSurnamePosition + 1
            }
            filter.toCharacterFilter(actualPosition)
        }

        return NameFilter(characterFilters, filterMode)
    }

    companion object {
        fun fromProfile(profile: Profile): DynamicFilterInput {
            val surnameFilters = profile.surname.mapIndexed { index, char ->
                FilterInput(
                    position = index,
                    isSurname = true,
                    hangul = char.toString(),
                    hanja = profile.surnameHanja.getOrNull(index)?.toString() ?: "",
                    filterType = FilterInputType.FIXED
                )
            }

            val givenNameFilters = List(2) { index ->
                FilterInput(
                    position = index,
                    isSurname = false,
                    filterType = FilterInputType.EMPTY
                )
            }

            return DynamicFilterInput(
                surnameFilters = surnameFilters,
                givenNameFilters = givenNameFilters
            )
        }
    }
}

data class FilterInput(
    val position: Int,
    val isSurname: Boolean,
    val hangul: String = "",
    val hanja: String = "",
    val filterType: FilterInputType = FilterInputType.EMPTY
) {
    fun toCharacterFilter(actualPosition: Int): CharacterFilter {
        return when (filterType) {
            FilterInputType.INITIAL_SOUND -> CharacterFilter(
                position = actualPosition,
                filterType = FilterType.INITIAL_SOUND,
                value = hangul
            )
            FilterInputType.CHARACTER -> CharacterFilter(
                position = actualPosition,
                filterType = FilterType.CHARACTER,
                value = hangul,
                hanja = hanja
            )
            FilterInputType.HANJA_ONLY -> CharacterFilter(
                position = actualPosition,
                filterType = FilterType.HANJA,
                hanja = hanja
            )
            FilterInputType.EMPTY -> CharacterFilter(
                position = actualPosition,
                filterType = FilterType.EMPTY
            )
            FilterInputType.FIXED -> CharacterFilter(
                position = actualPosition,
                filterType = FilterType.CHARACTER,
                value = hangul,
                hanja = hanja
            )
        }
    }
}

enum class FilterInputType {
    INITIAL_SOUND,  // 초성 조건
    CHARACTER,      // 글자 조건
    HANJA_ONLY,    // 한자 조건
    EMPTY,         // 빈칸 (자유)
    FIXED          // 고정 (성씨 등)
}