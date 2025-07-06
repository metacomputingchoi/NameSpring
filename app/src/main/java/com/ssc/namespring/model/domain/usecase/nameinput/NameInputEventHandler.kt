// model/domain/usecase/nameinput/NameInputEventHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
            private var debounceJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val korean = s?.toString() ?: ""
                val currentData = nameDataManager.getCharData(index)
                val currentHanja = currentData?.hanja ?: ""

                // 이전 디바운스 작업 취소
                debounceJob?.cancel()

                // 한글이 변경되고 기존 한자가 있었으면 초기화
                if (korean != previousText && currentHanja.isNotEmpty()) {
                    nameDataManager.setCharData(index, korean, "")
                    nameDataManager.removeHanjaInfo(index)
                    etHanja.setText("")
                    Toast.makeText(context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    nameDataManager.setCharData(index, korean, currentHanja)
                }

                // 디바운싱 없이 바로 업데이트 (또는 아주 짧은 딜레이)
                if (korean.isNotEmpty()) {
                    // 바로 업데이트
                    NameInputButtonUpdater.updateButtonText(
                        context,
                        btnSearchHanja,
                        korean,
                        etHanja.text.toString(),
                        index
                    )
                } else {
                    // 빈 값일 때는 바로 처리
                    btnSearchHanja.text = "한자 선택"
                    btnSearchHanja.setTextColor(context.getColor(R.color.text_secondary))
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

        stateManager.removeTextWatchers(index, etKorean, etHanja)
        etKorean.setText("")
        etHanja.setText("")
        nameDataManager.removeHanjaInfo(index)
        nameDataManager.setCharData(index, "", "")

        // 버튼 초기화
        NameInputButtonUpdater.updateButtonText(
            etKorean.context,
            btnSearchHanja,
            "",
            "",
            index
        )
    }

    fun cleanup() {
        // cleanup 시 모든 position의 job 취소
        for (i in 0..3) {
            NameInputButtonUpdater.cancelUpdate(i)
        }
    }
}