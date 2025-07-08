// model/domain/helper/NameDataHandler.kt
package com.ssc.namespring.model.domain.helper

import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.usecase.NameDataManager

/**
 * 이름 데이터 관련 작업을 처리하는 헬퍼 클래스
 */
class NameDataHandler(
    private val nameDataManager: NameDataManager,
    private val nameDataService: INameDataService
) {
    companion object {
        private const val TAG = "NameDataHandler"
    }

    private var isSettingHanjaInfo = false

    fun addCharIfPossible(): Boolean {
        if (nameDataManager.canAddChar()) {
            nameDataManager.addChar()
            return true
        }
        return false
    }

    fun removeCharIfPossible(): Boolean {
        if (nameDataManager.canRemoveChar()) {
            nameDataManager.removeChar()
            return true
        }
        return false
    }

    fun updateHanjaInfo(position: Int, korean: String, hanja: String): Boolean {
        if (isSettingHanjaInfo) return false

        val currentData = nameDataManager.getCharData(position)
        if (currentData?.korean == korean && currentData.hanja == hanja) {
            return false
        }

        isSettingHanjaInfo = true
        return try {
            // 빈 값도 허용하여 저장
            Log.d(TAG, "Updating char at $position: korean='$korean', hanja='$hanja'")
            nameDataManager.setCharData(position, korean, hanja)

            // 한글과 한자가 모두 있을 때만 charInfo 조회
            if (korean.isNotEmpty() && hanja.isNotEmpty()) {
                nameDataService.getCharInfo(korean, hanja)?.let { info ->
                    nameDataManager.setHanjaInfo(position, info)
                }
            } else {
                // 한자 정보가 불완전하면 제거
                nameDataManager.removeHanjaInfo(position)
            }
            true
        } finally {
            isSettingHanjaInfo = false
        }
    }

    fun syncWithUI(containerView: LinearLayout) {
        for (i in 0 until containerView.childCount) {
            val itemView = containerView.getChildAt(i)
            val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
            val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

            if (etKorean != null) {
                val korean = etKorean.text.toString()
                val hanja = etHanja?.text?.toString() ?: ""
                // 빈 값도 허용하여 저장
                nameDataManager.setCharData(i, korean, hanja)
            }
        }
    }
}