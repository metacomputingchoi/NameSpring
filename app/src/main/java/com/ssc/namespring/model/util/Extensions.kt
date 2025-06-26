// model/util/Extensions.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.hangul.HangulConstants
import java.text.Normalizer

fun String.normalizeNFC(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

fun Char.toHangulDecomposition(): Triple<Int, Int, Int> {
    val code = this.code - HangulConstants.HANGUL_BASE
    val cho = code / HangulConstants.INITIAL_COUNT
    val jung = (code / HangulConstants.MEDIAL_COUNT) % HangulConstants.MEDIALS_PER_INITIAL
    val jong = code % HangulConstants.MEDIAL_COUNT
    return Triple(cho, jung, jong)
}

fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
    return lists.fold(listOf(listOf())) { acc, list ->
        acc.flatMap { combination ->
            list.map { element -> combination + element }
        }
    }
}
