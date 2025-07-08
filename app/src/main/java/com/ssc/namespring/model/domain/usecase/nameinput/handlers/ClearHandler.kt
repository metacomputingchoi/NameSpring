// model/domain/usecase/nameinput/handlers/ClearHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput.handlers

import android.widget.Button
import android.widget.EditText
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputStateManager

class ClearHandler(
    private val nameDataManager: INameDataManager,
    private val stateManager: NameInputStateManager
) {
    fun clearInput(
        index: Int,
        etKorean: EditText,
        etHanja: EditText,
        btnSearchHanja: Button,
        onClearComplete: () -> Unit
    ) {
        // 업데이트 작업 취소
        NameInputButtonUpdater.cancelUpdate(index)

        // TextWatcher 제거
        stateManager.removeTextWatchers(index, etKorean, etHanja)

        // UI 초기화
        etKorean.setText("")
        etHanja.setText("")

        // NameDataManager 초기화
        nameDataManager.setCharData(index, "", "")
        nameDataManager.removeHanjaInfo(index)

        // 버튼 초기화
        btnSearchHanja.text = "한자 선택"
        btnSearchHanja.setTextColor(etKorean.context.getColor(R.color.text_secondary))

        // 완료 콜백 호출
        onClearComplete()
    }
}
