// ui/history/managers/NameListDataLoader.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.*

class NameListDataLoader(
    private val task: Task,
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope,
    private val onDataLoaded: (List<GeneratedName>) -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        private const val TAG = "NameListDataLoader"
    }

    private val gson = Gson()
    private var loadJob: Job? = null

    fun loadData() {
        loadJob = scope.launch {
            try {
                if (task.status != TaskStatus.COMPLETED) {
                    onError("작업이 아직 완료되지 않았습니다.")
                    return@launch
                }

                val result = taskRepository.getTaskResult(task.id)
                if (result == null) {
                    onError("결과 데이터를 찾을 수 없습니다.")
                    return@launch
                }

                val rawData = loadRawData(result)
                if (rawData == null) {
                    onError("이름 데이터를 찾을 수 없습니다.")
                    return@launch
                }

                val type = object : TypeToken<List<GeneratedName>>() {}.type
                val names: List<GeneratedName> = gson.fromJson(rawData, type)

                Log.d(TAG, "Loaded ${names.size} names")

                withContext(Dispatchers.Main) {
                    onDataLoaded(names)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                withContext(Dispatchers.Main) {
                    onError("데이터 로드 중 오류가 발생했습니다: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadRawData(result: com.ssc.namespring.model.domain.entity.TaskResult): String? {
        return if (result.rawData != null) {
            result.rawData
        } else if (result.data?.containsKey("rawDataFile") == true) {
            val filePath = result.data["rawDataFile"] as? String
            if (filePath != null) {
                withContext(Dispatchers.IO) {
                    taskRepository.loadRawDataFromFile(filePath)
                }
            } else null
        } else null
    }

    fun cancel() {
        loadJob?.cancel()
    }
}