// ui/history/components/namelist/utils/KoreanDecomposer.kt
package com.ssc.namespring.ui.history.components.namelist.utils

class KoreanDecomposer {
    fun decomposeIncompleteKorean(text: String): String {
        val result = StringBuilder()
        var i = 0

        while (i < text.length) {
            val char = text[i]

            if (char.code in 0xAC00..0xD7A3 && i + 1 < text.length) {
                val nextChar = text[i + 1]
                if (nextChar in 'ㄱ'..'ㅣ') {
                    result.append(char).append(' ').append(nextChar)
                    i += 2
                    continue
                }
            }

            result.append(char)
            i++
        }

        return result.toString()
    }
}