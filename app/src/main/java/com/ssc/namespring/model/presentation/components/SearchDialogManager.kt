// model/presentation/components/SearchDialogManager.kt
package com.ssc.namespring.model.presentation.components

import android.content.Context
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.presentation.components.search.SurnameSearchDialog
import com.ssc.namespring.model.presentation.components.search.HanjaSearchDialog

class SearchDialogManager {

    enum class SearchMode {
        ALL,      // 전체 검색 (음/뜻/한자 모두)
        SOUND,    // 음(音) 검색 (초성/한글)
        MEANING,  // 뜻 검색
        HANJA     // 한자 모양 검색
    }

    private val surnameSearchDialog = SurnameSearchDialog()
    private val hanjaSearchDialog = HanjaSearchDialog()

    fun showSurnameDialog(context: Context, onSurnameSelected: (SurnameInfo?) -> Unit) {
        surnameSearchDialog.show(context, onSurnameSelected)
    }

    fun showHanjaSearchDialog(
        context: Context,
        position: Int,
        initialKorean: String,
        onHanjaSelected: (Int, String, String) -> Unit
    ) {
        hanjaSearchDialog.show(context, position, initialKorean, onHanjaSelected)
    }
}