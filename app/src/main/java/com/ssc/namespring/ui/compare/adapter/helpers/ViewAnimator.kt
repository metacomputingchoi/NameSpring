// ui/compare/adapter/helpers/ViewAnimator.kt
package com.ssc.namespring.ui.compare.adapter.helpers

import android.view.View

/**
 * 뷰 애니메이션 처리기
 */
object ViewAnimator {
    fun animateClick(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}