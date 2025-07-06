// ui/profileform/ProfileFormNameInputHandler.kt
package com.ssc.namespring.ui.profileform

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ssc.namespring.model.domain.usecase.NameInputManager
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater

class ProfileFormNameInputHandler(
    private val formManager: ProfileFormManager,
    private val searchDialogManager: SearchDialogManager
) {
    companion object {
        private const val TAG = "ProfileFormNameInputHandler"
    }

    private var nameInputManager: NameInputManager? = null

    // ui/profileform/ProfileFormNameInputHandler.kt
    fun refreshNameInputViews(
        container: LinearLayout,
        state: ProfileFormUiState
    ) {
        Log.d(TAG, "refreshNameInputViews: charCount=${state.nameCharCount}")

        // 기존 뷰 제거만 하고 cleanup은 나중에
        container.removeAllViews()

        // NameInputManager 초기화 또는 재사용
        if (nameInputManager == null) {
            nameInputManager = NameInputManager(
                formManager.getNameDataManager()
            ) { position ->
                handleHanjaSearch(container.context, position)
            }
        }

        val context = container.context
        val inflater = LayoutInflater.from(context)

        // 각 글자 입력 뷰 생성
        state.nameCharDataList.forEachIndexed { index, charData ->
            Log.d(TAG, "Creating input view for position $index: korean='${charData.korean}', hanja='${charData.hanja}'")

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
        val koreanValue = currentData?.korean ?: ""

        Log.d(TAG, "Opening hanja search for position $position with korean: '$koreanValue'")

        searchDialogManager.showHanjaSearchDialog(
            context,
            position,
            koreanValue
        ) { pos, korean, hanja ->
            Log.d(TAG, "Hanja selected: position=$pos, korean='$korean', hanja='$hanja'")

            // 선택된 한자 정보를 가져와서 저장
            NameData.getCharInfo(korean, hanja)?.let { info ->
                Log.d(TAG, "Found CharTripleInfo for $korean/$hanja")
                // 한자 정보 설정
                formManager.getNameDataManager().setHanjaInfo(pos, info)
                // UI 업데이트를 위해 formManager 상태 업데이트
                formManager.setHanjaInfo(pos, korean, hanja)
            } ?: run {
                Log.w(TAG, "No CharTripleInfo found for $korean/$hanja")
                // CharTripleInfo가 없어도 기본 데이터는 업데이트
                formManager.setHanjaInfo(pos, korean, hanja)
            }
        }
    }

    fun cleanup() {
        Log.d(TAG, "cleanup()")
        nameInputManager?.cleanup()
        nameInputManager = null
        // cleanup은 Activity가 destroy될 때만 호출되도록
    }
}