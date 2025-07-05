// model/presentation/components/KoreanTextInputHandler.kt
package com.ssc.namespring.model.presentation.components

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.entity.NameData
import java.text.Normalizer

class KoreanTextInputHandler(
    private val index: Int,
    private val etHanja: EditText,
    private val btnSearchHanja: Button,
    private val nameDataManager: NameDataManager
) : TextWatcher {

    private var previousText = ""
    private var isInternalChange = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isInternalChange) return

        val text = s?.toString() ?: ""
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)

        if (text != normalized) {
            isInternalChange = true
            s?.replace(0, s.length, normalized)
            isInternalChange = false
            return
        }

        if (normalized.isNotEmpty() && !normalized.matches(Regex("^[가-힣ㄱ-ㅎㅏ-ㅣ]$"))) {
            isInternalChange = true
            s?.replace(0, s.length, previousText)
            isInternalChange = false
            Toast.makeText(etHanja.context, "한글만 입력 가능합니다", Toast.LENGTH_SHORT).show()
            return
        }

        if (previousText != normalized && etHanja.text.isNotEmpty()) {
            etHanja.setText("")
            nameDataManager.removeHanjaInfo(index)
            nameDataManager.setCharData(index, normalized, "")
            Toast.makeText(etHanja.context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            nameDataManager.setCharData(index, normalized, etHanja.text.toString())
        }

        updateButtonText(btnSearchHanja, normalized, etHanja.text.toString())
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