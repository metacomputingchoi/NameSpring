// model/presentation/components/search/HanjaSearchDialog.kt
package com.ssc.namespring.model.presentation.components.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import kotlinx.coroutines.*

internal class HanjaSearchDialog {

    private val searchController = HanjaSearchCoordinator()
    private val uiController = HanjaSearchUIHandler()

    fun show(
        context: Context,
        position: Int,
        initialKorean: String,
        onHanjaSelected: (Int, String, String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hanja_search, null)
        val hasKoreanConstraint = initialKorean.trim().isNotEmpty()
        val isChosung = initialKorean.matches(Regex("^[ㄱ-ㅎ]+$"))

        val adapter = HanjaSearchAdapter { result ->
            NameData.getCharInfo(result.tripleKey)?.let {
                onHanjaSelected(position, result.korean, result.hanja)
            }
        }

        val dialog = createDialog(context, dialogView, initialKorean, isChosung, hasKoreanConstraint)

        uiController.setupUI(
            dialogView,
            hasKoreanConstraint,
            isChosung,
            initialKorean,
            adapter
        )

        val searchScope = CoroutineScope(Dispatchers.Main)

        searchController.initialize(
            dialogView,
            adapter,
            hasKoreanConstraint,
            isChosung,
            initialKorean,
            searchScope
        ) { dialog.dismiss() }

        dialog.setOnDismissListener {
            searchScope.cancel()
        }

        dialog.show()
        searchController.loadBaseResults()
        uiController.initializeSearchField(dialogView)
    }

    private fun createDialog(
        context: Context,
        dialogView: View,
        initialKorean: String,
        isChosung: Boolean,
        hasKoreanConstraint: Boolean
    ): AlertDialog {
        val title = when {
            isChosung -> "초성 '$initialKorean' 한자 검색"
            hasKoreanConstraint -> "'$initialKorean' 한자 검색"
            else -> "한자 검색"
        }

        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()
    }
}