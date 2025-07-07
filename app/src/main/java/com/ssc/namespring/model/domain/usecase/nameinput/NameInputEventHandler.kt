// model/domain/usecase/nameinput/NameInputEventHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NameInputEventHandler(
    private val nameDataManager: INameDataManager,
    private val stateManager: NameInputStateManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {
    fun createKoreanTextWatcher(
        index: Int,
        etHanja: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return object : TextWatcher {
            private var previousText = ""
            private var isUpdating = false  // 플래그 추가

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return  // 업데이트 중이면 무시

                isUpdating = true
                try {
                    val korean = s?.toString() ?: ""
                    val currentData = nameDataManager.getCharData(index)
                    val currentHanja = currentData?.hanja ?: ""

                    if (korean != previousText && currentHanja.isNotEmpty()) {
                        nameDataManager.setCharData(index, korean, "")
                        nameDataManager.removeHanjaInfo(index)
                        etHanja.setText("")
                        Toast.makeText(context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        nameDataManager.setCharData(index, korean, currentHanja)
                    }

                    // 버튼 텍스트만 업데이트 (UI 전체 갱신 X)
                    if (korean.isNotEmpty()) {
                        NameInputButtonUpdater.updateButtonText(
                            context,
                            btnSearchHanja,
                            korean,
                            etHanja.text.toString(),
                            index
                        )
                    } else {
                        btnSearchHanja.text = "한자 선택"
                        btnSearchHanja.setTextColor(context.getColor(R.color.text_secondary))
                    }
                } finally {
                    isUpdating = false
                }
            }
        }
    }

    fun createHanjaTextWatcher(
        index: Int,
        etKorean: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val hanja = s?.toString() ?: ""
                val korean = etKorean.text.toString()
                nameDataManager.setCharData(index, korean, hanja)

                // 버튼 텍스트 업데이트 (position 전달)
                NameInputButtonUpdater.updateButtonText(
                    context,
                    btnSearchHanja,
                    korean,
                    hanja,
                    index
                )
            }
        }
    }

    // 누락된 메서드 추가
    fun handleHanjaSearchClick(index: Int) {
        onHanjaSearchClick(index)
    }

    fun handleClearClick(
        index: Int,
        etKorean: EditText,
        etHanja: EditText,
        btnSearchHanja: Button
    ) {
        // 업데이트 작업 취소
        NameInputButtonUpdater.cancelUpdate(index)

        // TextWatcher 제거
        stateManager.removeTextWatchers(index, etKorean, etHanja)

        // UI 초기화
        etKorean.setText("")
        etHanja.setText("")

        // ⭐ 중요: NameDataManager도 확실히 초기화
        nameDataManager.setCharData(index, "", "")
        nameDataManager.removeHanjaInfo(index)

        // 버튼 초기화
        btnSearchHanja.text = "한자 선택"
        btnSearchHanja.setTextColor(etKorean.context.getColor(R.color.text_secondary))

        // ⭐ TextWatcher 다시 등록
        val koreanWatcher = createKoreanTextWatcher(index, etHanja, btnSearchHanja, etKorean.context)
        val hanjaWatcher = createHanjaTextWatcher(index, etKorean, btnSearchHanja, etKorean.context)

        etKorean.addTextChangedListener(koreanWatcher)
        etHanja.addTextChangedListener(hanjaWatcher)

        stateManager.addTextWatchers(index, koreanWatcher, hanjaWatcher)
    }

    fun cleanup() {
        // cleanup 시 모든 position의 job 취소
        for (i in 0..3) {
            NameInputButtonUpdater.cancelUpdate(i)
        }
    }
}