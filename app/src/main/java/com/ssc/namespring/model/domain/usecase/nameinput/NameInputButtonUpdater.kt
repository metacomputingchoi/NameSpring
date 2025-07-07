// model/domain/usecase/nameinput/NameInputButtonUpdater.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.util.Log
import android.widget.Button
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import kotlinx.coroutines.*

object NameInputButtonUpdater {
    private const val TAG = "NameInputButtonUpdater"
    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()
    private val updateScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val activeJobs = mutableMapOf<Int, Job?>()

    // 프로필별 상태 추가
    private val profileStates = mutableMapOf<String, MutableMap<Int, String>>()

    fun cleanupForProfile(profileId: String?) {
        profileId?.let {
            profileStates.remove(it)
        }
    }

    fun updateButtonText(
        context: Context,
        button: Button,
        korean: String,
        hanja: String,
        position: Int,
        onUpdate: (() -> Unit)? = null
    ) {
        // 이전 작업 취소
        activeJobs[position]?.cancel()

        // 버튼 항상 활성화
        button.isEnabled = true

        when {
            // 이미 한자가 선택된 경우
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(context.getColor(R.color.primary))
                onUpdate?.invoke()
            }

            // 한글 입력이 없는 경우
            korean.isEmpty() -> {
                button.text = "한자 선택"
                button.setTextColor(context.getColor(R.color.text_secondary))
                onUpdate?.invoke()
            }

            // 한글이 입력된 경우
            else -> {
                // 즉시 검색 시작
                activeJobs[position] = updateScope.launch {
                    try {
                        val count = withContext(Dispatchers.IO) {
                            try {
                                when {
                                    // 초성인 경우
                                    korean.matches(Regex("^[ㄱ-ㅎ]+$")) -> {
                                        val results = nameDataService.searchHanja(korean)
                                        results.size
                                    }
                                    // 완전한 한글인 경우
                                    else -> {
                                        val allResults = nameDataService.searchHanja(korean)
                                        val exactMatches = allResults.filter { it.korean == korean }
                                        exactMatches.size
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "검색 중 예외 발생", e)
                                0
                            }
                        }

                        // UI 업데이트
                        withContext(Dispatchers.Main) {
                            if (isActive) {
                                val newText = if (count > 0) {
                                    "한자 선택 (${count}개)"
                                } else {
                                    "한자 선택"
                                }

                                button.text = newText
                                button.setTextColor(
                                    context.getColor(
                                        if (count > 0) R.color.primary
                                        else R.color.text_secondary
                                    )
                                )

                                onUpdate?.invoke()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "전체 프로세스 실패", e)
                    }
                }
            }
        }
    }

    fun cancelUpdate(position: Int) {
        activeJobs[position]?.cancel()
        activeJobs.remove(position)
    }

    fun cleanup() {
        activeJobs.values.forEach { it?.cancel() }
        activeJobs.clear()
        profileStates.clear()  // 상태도 정리
    }
}