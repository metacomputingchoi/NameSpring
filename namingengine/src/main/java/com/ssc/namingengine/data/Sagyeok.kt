// model/data/Sagyeok.kt
package com.ssc.namingengine.data

data class Sagyeok(
    val hyeong: Int,
    val won: Int,
    val i: Int,
    val jeong: Int
) {
    fun getValues() = listOf(hyeong, won, i, jeong)
}
