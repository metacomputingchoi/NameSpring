// model/domain/usecase/NameInputManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager

class NameInputManager(
    private val nameDataManager: INameDataManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {
    companion object {
        private const val TAG = "NameInputManager"
    }

    private val textWatchers = mutableMapOf<Int, Pair<TextWatcher, TextWatcher>>()

    fun createNameInputView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        index: Int
    ): View {
        val view = inflater.inflate(R.layout.item_name_input, parent, false)
        val data = nameDataManager.getCharData(index) ?: NameCharData()

        setupViews(view, index, data, context)

        return view
    }

    private fun setupViews(view: View, index: Int, data: NameCharData, context: Context) {
        val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
        val etKorean = view.findViewById<EditText>(R.id.etKorean)
        val etHanja = view.findViewById<EditText>(R.id.etHanja)
        val btnSearchHanja = view.findViewById<Button>(R.id.btnSearchHanja)
        val btnClearChar = view.findViewById<ImageButton>(R.id.btnClearChar)

        val positions = arrayOf("첫째", "둘째", "셋째", "넷째")
        tvPosition.text = positions[index]

        // 기존 TextWatcher 제거
        removeTextWatchers(index, etKorean, etHanja)

        // 초기값 설정 (TextWatcher 없이)
        etKorean.setText(data.korean)
        etHanja.setText(data.hanja)
        updateButtonText(btnSearchHanja, data.korean, data.hanja)

        // 새로운 TextWatcher 생성 및 추가
        val koreanWatcher = createKoreanTextWatcher(index, etHanja, btnSearchHanja, context)
        val hanjaWatcher = createHanjaTextWatcher(index, etKorean, btnSearchHanja)

        etKorean.addTextChangedListener(koreanWatcher)
        etHanja.addTextChangedListener(hanjaWatcher)

        textWatchers[index] = Pair(koreanWatcher, hanjaWatcher)

        // 버튼 리스너 설정
        btnSearchHanja.setOnClickListener {
            onHanjaSearchClick(index)
        }

        btnClearChar.setOnClickListener {
            removeTextWatchers(index, etKorean, etHanja)
            etKorean.setText("")
            etHanja.setText("")
            nameDataManager.removeHanjaInfo(index)
            nameDataManager.setCharData(index, "", "")
            updateButtonText(btnSearchHanja, "", "")
        }
    }

    private fun createKoreanTextWatcher(
        index: Int,
        etHanja: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return object : TextWatcher {
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val korean = s?.toString() ?: ""
                val currentData = nameDataManager.getCharData(index)
                val currentHanja = currentData?.hanja ?: ""

                // 한글이 변경되고 기존에 한자가 있었으면 한자 초기화
                if (korean != previousText && currentHanja.isNotEmpty()) {
                    nameDataManager.setCharData(index, korean, "")
                    nameDataManager.removeHanjaInfo(index)
                    etHanja.setText("")
                    Toast.makeText(context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    nameDataManager.setCharData(index, korean, currentHanja)
                }

                updateButtonText(btnSearchHanja, korean, etHanja.text.toString())
            }
        }
    }

    private fun createHanjaTextWatcher(
        index: Int,
        etKorean: EditText,
        btnSearchHanja: Button
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val hanja = s?.toString() ?: ""
                val korean = etKorean.text.toString()
                nameDataManager.setCharData(index, korean, hanja)
                updateButtonText(btnSearchHanja, korean, hanja)
            }
        }
    }

    private fun updateButtonText(button: Button, korean: String, hanja: String) {
        when {
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(button.context.getColor(R.color.primary))
            }
            korean.matches(Regex("^[ㄱ-ㅎ]+$")) -> {
                button.text = "초성 검색"
                button.setTextColor(button.context.getColor(R.color.text_secondary))
            }
            korean.length == 1 && korean.matches(Regex("[가-힣]")) -> {
                val results = NameData.searchHanja(korean).filter { it.korean == korean }
                button.text = if (results.isNotEmpty()) {
                    "선택: ${results.size}개"
                } else {
                    "한자 없음"
                }
                button.setTextColor(button.context.getColor(R.color.text_secondary))
            }
            else -> {
                button.text = "한자 검색"
                button.setTextColor(button.context.getColor(R.color.text_secondary))
            }
        }
    }

    private fun removeTextWatchers(index: Int, etKorean: EditText, etHanja: EditText) {
        textWatchers[index]?.let { (koreanWatcher, hanjaWatcher) ->
            etKorean.removeTextChangedListener(koreanWatcher)
            etHanja.removeTextChangedListener(hanjaWatcher)
        }
        textWatchers.remove(index)
    }

    fun cleanup() {
        textWatchers.clear()
    }
}