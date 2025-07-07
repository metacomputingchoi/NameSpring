// ui/history/components/taskdetail/TaskDetailComponentBuilder.kt
package com.ssc.namespring.ui.history.components.taskdetail

import android.content.Context
import android.view.View
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R

class TaskDetailComponentBuilder(
    private val context: Context
) {
    fun createTitleView(title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 20f
            setTextColor(context.getColor(android.R.color.black))
            setPadding(0, 0, 0, 16)
        }
    }

    fun createSectionTitle(title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 16f
            setTextColor(context.getColor(R.color.primary_blue))
            setPadding(0, 24, 0, 8)
        }
    }

    fun createInfoRow(label: String, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4, 0, 4)

            if (label.isNotEmpty()) {
                addView(TextView(context).apply {
                    text = "$label: "
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }

            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(context.getColor(android.R.color.black))
            })
        }
    }

    fun createCodeView(code: String): HorizontalScrollView {
        return HorizontalScrollView(context).apply {
            addView(TextView(context).apply {
                text = code
                textSize = 12f
                setTextColor(context.getColor(android.R.color.black))
                setBackgroundColor(context.getColor(android.R.color.darker_gray))
                setPadding(16, 16, 16, 16)
                setTextIsSelectable(true)
            })
        }
    }

    fun createErrorView(error: String): TextView {
        return TextView(context).apply {
            text = error
            textSize = 14f
            setTextColor(context.getColor(R.color.error_red))
            setPadding(0, 8, 0, 8)
        }
    }

    fun createDivider(): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(context.getColor(android.R.color.darker_gray))
        }
    }

    fun createButtonsLayout(onDismiss: () -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END

            addView(MaterialButton(context).apply {
                text = "닫기"
                setOnClickListener { onDismiss() }
            })
        }
    }
}