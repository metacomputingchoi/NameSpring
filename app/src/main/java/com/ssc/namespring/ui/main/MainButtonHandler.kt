// ui/main/MainButtonHandler.kt
package com.ssc.namespring.ui.main

import android.content.Context
import android.os.Handler
import android.view.HapticFeedbackConstants
import android.view.View

class MainButtonHandler(
    private val context: Context,
    private val handler: Handler
) {
    fun handleButtonClick(
        view: View, 
        delayMillis: Long = 500,
        action: () -> Unit
    ) {
        // 즉시 시각적 피드백
        view.isEnabled = false
        view.alpha = 0.6f
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        // 액션 수행
        action()

        // 버튼 상태 복구
        handler.postDelayed({
            view.isEnabled = true
            view.alpha = 1.0f
        }, delayMillis)
    }
}