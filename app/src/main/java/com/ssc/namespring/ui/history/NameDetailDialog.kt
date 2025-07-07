// ui/history/NameDetailDialog.kt
package com.ssc.namespring.ui.history

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.gson.GsonBuilder
import com.ssc.namespring.R
import com.ssc.namingengine.data.GeneratedName

class NameDetailDialog(
    context: Context,
    private val name: GeneratedName
) : Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert) {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_name_detail)

        setupViews()
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvTitle).text =
            "${name.combinedPronounciation} (${name.combinedHanja})"

        findViewById<TextView>(R.id.tvJsonContent).text = gson.toJson(name)

        findViewById<MaterialButton>(R.id.btnCopy).setOnClickListener {
            copyToClipboard()
        }

        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Name JSON", gson.toJson(name))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "JSON이 클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
    }
}