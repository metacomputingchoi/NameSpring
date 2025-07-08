// ui/history/data/NameListRawRetLoader.kt
package com.ssc.namespring.ui.history.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NameListRawRetLoader(
    private val taskRepository: TaskRepository,
    private val gson: Gson = Gson()
) {

    suspend fun loadNames(task: Task): List<GeneratedName> = withContext(Dispatchers.IO) {
        // 작업이 완료 상태인지 확인
        if (task.status != TaskStatus.COMPLETED) {
            throw IllegalStateException("작업이 아직 완료되지 않았습니다.")
        }

        // TaskResult 가져오기
        val result = taskRepository.getTaskResult(task.id)
            ?: throw IllegalStateException("결과 데이터를 찾을 수 없습니다.")

        // Raw data 로드
        val rawData = loadRawData(result)
            ?: throw IllegalStateException("이름 데이터를 찾을 수 없습니다.")

        // GeneratedName 리스트로 파싱
        val type = object : TypeToken<List<GeneratedName>>() {}.type
        gson.fromJson<List<GeneratedName>>(rawData, type)
    }

    private suspend fun loadRawData(result: com.ssc.namespring.model.domain.entity.TaskResult): String? {
        return if (result.rawData != null) {
            result.rawData
        } else if (result.data?.containsKey("rawDataFile") == true) {
            val filePath = result.data["rawDataFile"] as? String
            filePath?.let { taskRepository.loadRawDataFromFile(it) }
        } else {
            null
        }
    }
}