// model/domain/usecase/nameinput/NameInputButtonUpdater.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.widget.Button
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.NameData

object NameInputButtonUpdater {
    fun updateButtonText(
        context: Context,
        button: Button,
        korean: String,
        hanja: String
    ) {
        when {
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(context.getColor(R.color.primary))
            }
            korean.matches(Regex("^[ㄱ-ㅎ]+$")) -> {
                button.text = "초성 검색"
                button.setTextColor(context.getColor(R.color.text_secondary))
            }
            korean.length == 1 && korean.matches(Regex("[가-힣]")) -> {
                val results = NameData.searchHanja(korean).filter { it.korean == korean }
                button.text = if (results.isNotEmpty()) {
                    "선택: ${results.size}개"
                } else {
                    "한자 없음"
                }
                button.setTextColor(context.getColor(R.color.text_secondary))
            }
            else -> {
                button.text = "한자 검색"
                button.setTextColor(context.getColor(R.color.text_secondary))
            }
        }
    }
}