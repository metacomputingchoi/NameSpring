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
    private var isRefreshing = false // 중복 실행 방지 플래그

    fun refreshNameInputViews(
        container: LinearLayout,
        state: ProfileFormUiState,
        forceRecreate: Boolean = false
    ) {
        Log.d(TAG, "refreshNameInputViews: charCount=${state.nameCharCount}, forceRecreate=$forceRecreate")

        // 이미 갱신 중이면 스킵
        if (isRefreshing) {
            Log.d(TAG, "Already refreshing, skip")
            return
        }

        isRefreshing = true
        container.removeAllViews()

        // 프로필 로드 시 완전 초기화
        if (forceRecreate) {
            cleanup()  // 모든 것을 정리
            // postDelayed 제거하고 바로 실행
            recreateViews(container, state)
        } else {
            recreateViews(container, state)
        }

        isRefreshing = false
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
        isRefreshing = false
    }
}