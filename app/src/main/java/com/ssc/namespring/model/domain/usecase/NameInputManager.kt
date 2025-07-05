// model/domain/usecase/NameInputManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.presentation.components.SimpleHanjaTextWatcher
import com.ssc.namespring.model.presentation.components.SimpleKoreanTextWatcher
import com.ssc.namespring.model.domain.entity.NameData
import java.lang.ref.WeakReference

class NameInputManager(
    private val nameDataManager: NameDataManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {

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

        etKorean.setText(data.korean)
        etHanja.setText(data.hanja)
        updateButtonText(btnSearchHanja, data.korean, data.hanja)

        // WeakReference를 사용하여 메모리 누수 방지
        val weakContext = WeakReference(context)

        val koreanWatcher = SimpleKoreanTextWatcher(index) { pos, korean ->
            val ctx = weakContext.get() ?: return@SimpleKoreanTextWatcher
            val hanjaText = etHanja.text.toString()

            if (korean != data.korean && hanjaText.isNotEmpty()) {
                etHanja.setText("")
                nameDataManager.removeHanjaInfo(pos)
                Toast.makeText(ctx, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
            }

            nameDataManager.setCharData(pos, korean, etHanja.text.toString())
            updateButtonText(btnSearchHanja, korean, etHanja.text.toString())
        }

        val hanjaWatcher = SimpleHanjaTextWatcher(index) { pos, hanja ->
            nameDataManager.setCharData(pos, etKorean.text.toString(), hanja)
            updateButtonText(btnSearchHanja, etKorean.text.toString(), hanja)
        }

        etKorean.addTextChangedListener(koreanWatcher)
        etHanja.addTextChangedListener(hanjaWatcher)

        btnSearchHanja.setOnClickListener { onHanjaSearchClick(index) }
        btnClearChar.setOnClickListener {
            etKorean.setText("")
            etHanja.setText("")
            nameDataManager.removeHanjaInfo(index)
            nameDataManager.setCharData(index, "", "")
        }
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