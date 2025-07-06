// model/domain/entity/NameCharacter.kt
package com.ssc.namespring.model.domain.entity

data class NameCharacter(
    val position: Int,
    val korean: String = "",
    val hanja: String = "",
    val charInfo: CharInfo? = null
) {
    fun isEmpty(): Boolean = korean.isEmpty() && hanja.isEmpty()

    fun isComplete(): Boolean = korean.isNotEmpty() && hanja.isNotEmpty()

    fun withKorean(newKorean: String): NameCharacter =
        copy(korean = newKorean)

    fun withHanja(newHanja: String): NameCharacter =
        copy(hanja = newHanja)

    fun withCharInfo(info: CharInfo?): NameCharacter =
        copy(charInfo = info)

    fun clearHanja(): NameCharacter =
        copy(hanja = "", charInfo = null)
}