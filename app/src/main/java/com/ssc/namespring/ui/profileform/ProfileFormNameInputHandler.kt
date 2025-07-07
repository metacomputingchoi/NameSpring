// ui/profileform/ProfileFormNameInputHandler.kt
package com.ssc.namespring.ui.profileform

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.R
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

    fun refreshNameInputViews(
        container: LinearLayout,
        state: ProfileFormUiState,
        forceRecreate: Boolean = false
    ) {
        Log.d(TAG, "refreshNameInputViews: charCount=${state.nameCharCount}, forceRecreate=$forceRecreate")

        container.removeAllViews()

        // 프로필 로드 시 완전 초기화
        if (forceRecreate) {
            cleanup()  // 모든 것을 정리
            container.postDelayed({
                recreateViews(container, state)
            }, 100)  // 약간의 지연 후 재생성
        } else {
            recreateViews(container, state)
        }
    }

    private fun recreateViews(container: LinearLayout, state: ProfileFormUiState) {
        if (nameInputManager == null) {
            nameInputManager = NameInputManager(
                formManager.getNameDataManager()
            ) { position ->
                handleHanjaSearch(container.context, position)
            }
        }

        val context = container.context
        val inflater = LayoutInflater.from(context)

        state.nameCharDataList.forEachIndexed { index, charData ->
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
        // 문제: formManager.getNameDataManager().getCharData(position)를 사용
        // 이것은 x버튼으로 UI를 초기화해도 여전히 이전 값을 가지고 있음

        // 해결: 현재 UI의 EditText에서 직접 값을 가져와야 함
        val dataFromManager = formManager.getNameDataManager().getCharData(position)
        Log.d(TAG, "Data from Manager - korean: '${dataFromManager?.korean}', hanja: '${dataFromManager?.hanja}'")

        // UI에서 직접 가져온 값
        val container = (context as? ProfileFormActivity)?.findViewById<LinearLayout>(R.id.nameInputContainer)
        val itemView = container?.getChildAt(position)
        val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
        val uiValue = etKorean?.text?.toString() ?: ""

        Log.d(TAG, "UI EditText value: '$uiValue'")

        // UI 값 사용
        searchDialogManager.showHanjaSearchDialog(
            context,
            position,
            uiValue  // UI 값 사용!
        ) { pos, korean, hanja ->
            Log.d(TAG, "Hanja selected: position=$pos, korean='$korean', hanja='$hanja'")

            NameData.getCharInfo(korean, hanja)?.let { info ->
                Log.d(TAG, "Found CharTripleInfo for $korean/$hanja")
                formManager.getNameDataManager().setHanjaInfo(pos, info)
                formManager.setHanjaInfo(pos, korean, hanja)
            } ?: run {
                Log.w(TAG, "No CharTripleInfo found for $korean/$hanja")
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