// ui/profileform/ProfileFormNameInputHandler.kt
package com.ssc.namespring.ui.profileform

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ssc.namespring.model.domain.usecase.NameInputManager
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.model.domain.entity.NameData

class ProfileFormNameInputHandler(
    private val formManager: ProfileFormManager,
    private val searchDialogManager: SearchDialogManager
) {
    private var nameInputManager: NameInputManager? = null

    fun refreshNameInputViews(
        container: LinearLayout,
        state: ProfileFormUiState
    ) {
        container.removeAllViews()

        if (nameInputManager == null) {
            nameInputManager = NameInputManager(
                formManager.getNameDataManager()
            ) { position ->
                handleHanjaSearch(container.context, position)
            }
        }

        val context = container.context
        val inflater = LayoutInflater.from(context)

        state.nameCharDataList.forEachIndexed { index, _ ->
            nameInputManager?.let { manager ->
                val itemView = manager.createNameInputView(
                    context,
                    inflater,
                    container,
                    index
                )
                container.addView(itemView)
            }
        }
    }

    private fun handleHanjaSearch(context: Context, position: Int) {
        val currentData = formManager.getNameDataManager().getCharData(position)
        val initialQuery = currentData?.korean ?: ""

        searchDialogManager.showHanjaSearchDialog(
            context,
            position,
            initialQuery
        ) { pos, korean, hanja ->
            formManager.setHanjaInfo(pos, korean, hanja)

            NameData.getCharInfo(korean, hanja)?.let { info ->
                formManager.getNameDataManager().setHanjaInfo(pos, info)
            }
        }
    }

    fun cleanup() {
        nameInputManager = null
    }
}