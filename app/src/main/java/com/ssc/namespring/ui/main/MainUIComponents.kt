// ui/main/MainUIComponents.kt
package com.ssc.namespring.ui.main

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class MainUIComponents(activity: Activity) {
    private val viewRefs = MainViewReferences(activity)
    private val buttonFinder = MainButtonViewFinder()

    // View References 위임
    val rootLayout: ConstraintLayout get() = viewRefs.rootLayout
    val tvProfileLabel: TextView get() = viewRefs.tvProfileLabel
    val tvScore: TextView get() = viewRefs.tvScore
    val tvScoreIcon: TextView get() = viewRefs.tvScoreIcon
    val scoreContainer: LinearLayout get() = viewRefs.scoreContainer
    val tvName: TextView get() = viewRefs.tvName
    val tvBirthInfo: TextView get() = viewRefs.tvBirthInfo
    val tvOhaengInfo: TextView get() = viewRefs.tvOhaengInfo
    val ohaengContainers: List<LinearLayout> get() = viewRefs.ohaengContainers
    val ohaengCounts: List<TextView> get() = viewRefs.ohaengCounts
    val btnNaming: CardView get() = viewRefs.btnNaming
    val btnEvaluation: CardView get() = viewRefs.btnEvaluation
    val btnCompare: CardView get() = viewRefs.btnCompare
    val btnHistory: CardView get() = viewRefs.btnHistory
    val serviceButtons: List<CardView> get() = viewRefs.serviceButtons

    // 버튼 내부 TextView들
    val tvNamingText: TextView = buttonFinder.findTextView(btnNaming, "작명")
    val tvEvaluationText: TextView = buttonFinder.findTextView(btnEvaluation, "평가")
    val tvCompareText: TextView = buttonFinder.findTextView(btnCompare, "비교")
    val tvHistoryText: TextView = buttonFinder.findTextView(btnHistory, "기록")

    val tvNamingIcon: TextView = buttonFinder.findIconView(btnNaming)
    val tvEvaluationIcon: TextView = buttonFinder.findIconView(btnEvaluation)
    val tvCompareIcon: TextView = buttonFinder.findIconView(btnCompare)
    val tvHistoryIcon: TextView = buttonFinder.findIconView(btnHistory)

    fun updateButtonText(buttonType: ButtonType, text: String) {
        when (buttonType) {
            ButtonType.NAMING -> tvNamingText.text = text
            ButtonType.EVALUATION -> tvEvaluationText.text = text
            ButtonType.COMPARE -> tvCompareText.text = text
            ButtonType.HISTORY -> tvHistoryText.text = text
        }
    }

    fun updateButtonIcon(buttonType: ButtonType, emoji: String) {
        when (buttonType) {
            ButtonType.NAMING -> tvNamingIcon.text = emoji
            ButtonType.EVALUATION -> tvEvaluationIcon.text = emoji
            ButtonType.COMPARE -> tvCompareIcon.text = emoji
            ButtonType.HISTORY -> tvHistoryIcon.text = emoji
        }
    }

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