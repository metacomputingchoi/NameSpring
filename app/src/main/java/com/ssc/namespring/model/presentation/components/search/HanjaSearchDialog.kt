// model/presentation/components/search/HanjaSearchDialog.kt
package com.ssc.namespring.model.presentation.components.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import kotlinx.coroutines.*

internal class HanjaSearchDialog {
    companion object {
        private const val TAG = "HanjaSearchDialog"
    }

    private val searchController = HanjaSearchCoordinator()
    private val uiController = HanjaSearchUIHandler()
    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()

    fun show(
        context: Context,
        position: Int,
        initialKorean: String,
        onHanjaSelected: (Int, String, String) -> Unit
    ) {
        Log.d(TAG, "show: position=$position, initialKorean='$initialKorean'")

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hanja_search, null)
        val hasKoreanConstraint = initialKorean.trim().isNotEmpty()
        val isChosung = initialKorean.matches(Regex("^[ㄱ-ㅎ]+$"))

        val adapter = HanjaSearchAdapter { result ->
            Log.d(TAG, "Hanja selected: ${result.korean}/${result.hanja}")

            // 한자 정보가 존재하는지 확인
            val charInfo = nameDataService.getCharInfo(result.tripleKey)
            if (charInfo != null) {
                Log.d(TAG, "CharInfo found for ${result.korean}/${result.hanja}")
                onHanjaSelected(position, result.korean, result.hanja)
            } else {
                Log.e(TAG, "CharInfo not found for tripleKey: ${result.tripleKey}")
                // tripleKey로 못찾으면 korean/hanja로 다시 시도
                val altCharInfo = nameDataService.getCharInfo(result.korean, result.hanja)
                if (altCharInfo != null) {
                    Log.d(TAG, "CharInfo found with korean/hanja: ${result.korean}/${result.hanja}")
                    onHanjaSelected(position, result.korean, result.hanja)
                } else {
                    Log.e(TAG, "CharInfo not found at all")
                    // 그래도 기본 데이터는 전달
                    onHanjaSelected(position, result.korean, result.hanja)
                }
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
            searchScope,
            nameDataService
        ) {
            Log.d(TAG, "Dialog dismissed after selection")
            dialog.dismiss()
        }

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
            initialKorean.isEmpty() -> "한자 선택"
            isChosung -> "'$initialKorean' 초성 한자"
            hasKoreanConstraint -> "'$initialKorean' 한자"
            else -> "한자 선택"
        }

        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()
    }
}