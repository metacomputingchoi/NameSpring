// model/presentation/components/HanjaTextInputHandler.kt
package com.ssc.namespring.model.presentation.components

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.entity.NameData

class HanjaTextInputHandler(
    private val index: Int,
    private val etKorean: EditText,
    private val btnSearchHanja: Button,
    private val nameDataManager: NameDataManager
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val text = s?.toString() ?: ""
        nameDataManager.setCharData(index, etKorean.text.toString(), text)
        updateButtonText(btnSearchHanja, etKorean.text.toString(), text)
    }

    private fun updateButtonText(button: Button, korean: String, hanja: String) {
        when {
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(button.context.getColor(R.color.white))
            }
            korean.length == 1 && korean.matches(Regex("[가-힣]")) -> {
                val results = NameData.searchHanja(korean)
                button.text = if (results.isNotEmpty()) {
                    "예시: ${results[0].hanja}"
                } else {
                    "한자 검색"
                }
                button.setTextColor(button.context.getColor(R.color.white))
            }
            else -> {
                button.text = "한자 검색"
                button.setTextColor(button.context.getColor(R.color.white))
            }
        }
    }
}