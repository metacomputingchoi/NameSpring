// ui/compare/CompareSelectionDialog.kt
package com.ssc.namespring.ui.compare

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName

class CompareSelectionDialog(
    context: Context,
    private val selectedFavorites: List<FavoriteName>
) : Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_compare_selection)

        setupViews()
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvTitle).text =
            "${selectedFavorites.size}개 이름 비교"

        val container = findViewById<LinearLayout>(R.id.containerNames)

        selectedFavorites.forEach { favorite ->
            val nameView = layoutInflater.inflate(
                R.layout.item_compare_name_detail,
                container,
                false
            )

            nameView.findViewById<TextView>(R.id.tvNameTitle).text =
                "${favorite.fullNameKorean} (${favorite.fullNameHanja})"

            nameView.findViewById<TextView>(R.id.tvJsonData).text =
                formatJson(favorite.jsonData)

            container.addView(nameView)
        }

        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun formatJson(json: String): String {
        // JSON을 보기 좋게 포맷팅
        return try {
            com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    com.google.gson.JsonParser.parseString(json)
                )
        } catch (e: Exception) {
            json
        }
    }
}