// model/util/Extensions.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.Constants.HANGUL_BASE
import com.ssc.namespring.model.common.Constants.INITIAL_COUNT
import com.ssc.namespring.model.common.Constants.MEDIAL_COUNT
import com.ssc.namespring.model.common.Constants.MEDIALS_PER_INITIAL
import java.text.Normalizer

fun String.normalizeNFC(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

fun Char.toHangulDecomposition(): Triple<Int, Int, Int> {
    val code = this.code - HANGUL_BASE
    val cho = code / INITIAL_COUNT
    val jung = (code / MEDIAL_COUNT) % MEDIALS_PER_INITIAL
    val jong = code % MEDIAL_COUNT
    return Triple(cho, jung, jong)
}

fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
    return lists.fold(listOf(listOf())) { acc, list ->
        acc.flatMap { combination ->
            list.map { element -> combination + element }
        }
    }
}