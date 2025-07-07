// model/domain/usecase/nameinput/NameInputUIFactory.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.components.NameCharData

class NameInputUIFactory(
    private val eventHandler: NameInputEventHandler,
    private val stateManager: NameInputStateManager
) {
    fun createNameInputView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        index: Int,
        data: NameCharData?
    ): View {
        val view = inflater.inflate(R.layout.item_name_input, parent, false)

        setupViews(view, index, data ?: NameCharData(), context)

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

        // 기존 리스너 제거
        stateManager.removeTextWatchers(index, etKorean, etHanja)

        // TextWatcher 생성
        val koreanWatcher = eventHandler.createKoreanTextWatcher(index, etHanja, btnSearchHanja, context)
        val hanjaWatcher = eventHandler.createHanjaTextWatcher(index, etKorean, btnSearchHanja, context)

        // 리스너 먼저 등록
        etKorean.addTextChangedListener(koreanWatcher)
        etHanja.addTextChangedListener(hanjaWatcher)
        stateManager.addTextWatchers(index, koreanWatcher, hanjaWatcher)

        // 값 설정은 리스너 등록 후에
        etKorean.setText(data.korean)
        etHanja.setText(data.hanja)

        // 프로필 로드 후 버튼 강제 업데이트
        etKorean.post {
            NameInputButtonUpdater.updateButtonText(
                context,
                btnSearchHanja,
                data.korean,
                data.hanja,
                index
            )
        }

        btnSearchHanja.setOnClickListener {
            eventHandler.handleHanjaSearchClick(index)
        }

        btnClearChar.setOnClickListener {
            eventHandler.handleClearClick(index, etKorean, etHanja, btnSearchHanja)
        }
    }
}