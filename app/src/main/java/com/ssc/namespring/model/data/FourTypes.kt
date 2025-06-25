// model/data/FourTypes.kt
package com.ssc.namespring.model.data

data class FourTypes(
    val hyung: Int,
    val won: Int,
    val i: Int,
    val jung: Int
) {
    fun getValues() = listOf(hyung, won, i, jung)
}
