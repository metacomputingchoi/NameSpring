// ui/main/MainButtonViewFinder.kt
package com.ssc.namespring.ui.main

import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

class MainButtonViewFinder {
    fun findTextView(cardView: CardView, defaultText: String): TextView {
        val linearLayout = cardView.getChildAt(0) as? LinearLayout
            ?: throw IllegalStateException("CardView must contain LinearLayout")

        val textView = linearLayout.getChildAt(1) as? TextView
            ?: throw IllegalStateException("LinearLayout must contain TextView at index 1")

        return textView
    }

    fun findIconView(cardView: CardView): TextView {
        val linearLayout = cardView.getChildAt(0) as? LinearLayout
            ?: throw IllegalStateException("CardView must contain LinearLayout")

        val iconView = linearLayout.getChildAt(0) as? TextView
            ?: throw IllegalStateException("LinearLayout must contain TextView at index 0")

        return iconView
    }
}