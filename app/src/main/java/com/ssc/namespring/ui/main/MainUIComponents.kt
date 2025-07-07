// ui/main/MainUIComponents.kt
package com.ssc.namespring.ui.main

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssc.namespring.R

class MainUIComponents(activity: Activity) {
    val rootLayout: ConstraintLayout = activity.findViewById(R.id.rootLayout)
    val tvProfileLabel: TextView = activity.findViewById(R.id.tvProfileLabel)
    val tvScore: TextView = activity.findViewById(R.id.tvScore)
    val tvScoreIcon: TextView = activity.findViewById(R.id.tvScoreIcon)
    val scoreContainer: LinearLayout = activity.findViewById(R.id.scoreContainer)
    val tvName: TextView = activity.findViewById(R.id.tvName)
    val tvBirthInfo: TextView = activity.findViewById(R.id.tvBirthInfo)
    val tvOhaengInfo: TextView = activity.findViewById(R.id.tvOhaengInfo)

    val ohaengContainers: List<LinearLayout> = listOf(
        activity.findViewById(R.id.containerWood),
        activity.findViewById(R.id.containerFire),
        activity.findViewById(R.id.containerEarth),
        activity.findViewById(R.id.containerMetal),
        activity.findViewById(R.id.containerWater)
    )

    val ohaengCounts: List<TextView> = listOf(
        activity.findViewById(R.id.tvWoodCount),
        activity.findViewById(R.id.tvFireCount),
        activity.findViewById(R.id.tvEarthCount),
        activity.findViewById(R.id.tvMetalCount),
        activity.findViewById(R.id.tvWaterCount)
    )

    val btnNaming: CardView = activity.findViewById(R.id.btnNaming)
    val btnEvaluation: CardView = activity.findViewById(R.id.btnEvaluation)
    val btnCompare: CardView = activity.findViewById(R.id.btnCompare)
    val btnHistory: CardView = activity.findViewById(R.id.btnHistory)

    val serviceButtons: List<CardView> = listOf(btnNaming, btnEvaluation, btnCompare, btnHistory)

    // 버튼 내부의 텍스트 TextView들을 찾기
    val tvNamingText: TextView = findButtonTextView(btnNaming, "작명")
    val tvEvaluationText: TextView = findButtonTextView(btnEvaluation, "평가")
    val tvCompareText: TextView = findButtonTextView(btnCompare, "비교")
    val tvHistoryText: TextView = findButtonTextView(btnHistory, "기록")

    // 버튼 내부의 아이콘 TextView들을 찾기 (선택사항)
    val tvNamingIcon: TextView = findButtonIconView(btnNaming)
    val tvEvaluationIcon: TextView = findButtonIconView(btnEvaluation)
    val tvCompareIcon: TextView = findButtonIconView(btnCompare)
    val tvHistoryIcon: TextView = findButtonIconView(btnHistory)

    /**
     * CardView 내부에서 텍스트를 표시하는 TextView를 찾는다
     * CardView > LinearLayout > 두 번째 TextView (텍스트)
     */
    private fun findButtonTextView(cardView: CardView, defaultText: String): TextView {
        val linearLayout = cardView.getChildAt(0) as? LinearLayout
            ?: throw IllegalStateException("CardView must contain LinearLayout")

        // LinearLayout의 두 번째 자식이 텍스트 TextView
        val textView = linearLayout.getChildAt(1) as? TextView
            ?: throw IllegalStateException("LinearLayout must contain TextView at index 1")

        // 기본 텍스트 확인 (옵션)
        if (textView.text.toString() != defaultText) {
            // 로그 또는 경고 (필요시)
        }

        return textView
    }

    /**
     * CardView 내부에서 아이콘을 표시하는 TextView를 찾는다
     * CardView > LinearLayout > 첫 번째 TextView (아이콘)
     */
    private fun findButtonIconView(cardView: CardView): TextView {
        val linearLayout = cardView.getChildAt(0) as? LinearLayout
            ?: throw IllegalStateException("CardView must contain LinearLayout")

        // LinearLayout의 첫 번째 자식이 아이콘 TextView
        val iconView = linearLayout.getChildAt(0) as? TextView
            ?: throw IllegalStateException("LinearLayout must contain TextView at index 0")

        return iconView
    }

    /**
     * 특정 버튼의 텍스트를 업데이트한다
     */
    fun updateButtonText(buttonType: ButtonType, text: String) {
        when (buttonType) {
            ButtonType.NAMING -> tvNamingText.text = text
            ButtonType.EVALUATION -> tvEvaluationText.text = text
            ButtonType.COMPARE -> tvCompareText.text = text
            ButtonType.HISTORY -> tvHistoryText.text = text
        }
    }

    /**
     * 특정 버튼의 아이콘을 업데이트한다
     */
    fun updateButtonIcon(buttonType: ButtonType, emoji: String) {
        when (buttonType) {
            ButtonType.NAMING -> tvNamingIcon.text = emoji
            ButtonType.EVALUATION -> tvEvaluationIcon.text = emoji
            ButtonType.COMPARE -> tvCompareIcon.text = emoji
            ButtonType.HISTORY -> tvHistoryIcon.text = emoji
        }
    }

    /**
     * 모든 버튼 텍스트를 기본값으로 재설정한다
     */
    fun resetAllButtonTexts() {
        tvNamingText.text = "작명"
        tvEvaluationText.text = "평가"
        tvCompareText.text = "비교"
        tvHistoryText.text = "기록"
    }

    enum class ButtonType {
        NAMING, EVALUATION, COMPARE, HISTORY
    }
}