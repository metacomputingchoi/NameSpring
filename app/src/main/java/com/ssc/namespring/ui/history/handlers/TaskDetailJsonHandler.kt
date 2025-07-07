// ui/history/handlers/TaskDetailJsonHandler.kt
package com.ssc.namespring.ui.history.handlers

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class TaskDetailJsonHandler(
    private val context: Context,
    private val gson: Gson
) {
    fun createRawDataButtons(rawData: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)

            addView(MaterialButton(context).apply {
                text = "JSON 전체 보기"
                setOnClickListener {
                    showFullJsonDialog(rawData)
                }
            })

            addView(MaterialButton(context).apply {
                text = "JSON 복사"
                setOnClickListener {
                    copyToClipboard(rawData)
                }
            })
        }
    }

    private fun showFullJsonDialog(jsonData: String) {
        val dialog = Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert)

        val scrollView = ScrollView(context)
        val textView = TextView(context).apply {
            text = jsonData
            setPadding(32, 32, 32, 32)
            textSize = 12f
            setTextIsSelectable(true)
        }

        scrollView.addView(textView)
        dialog.setContentView(scrollView)
        dialog.setTitle("전체 JSON 데이터")

        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Task Result JSON", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "JSON이 클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
    }
}