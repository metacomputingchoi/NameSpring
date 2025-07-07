// model/data/repository/task/TaskFileService.kt
package com.ssc.namespring.model.data.repository.task

import android.content.Context
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.task.handlers.*
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.data.repository.task.services.FileCleanupService
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.sync.Mutex
import java.io.File

class TaskFileService(
    private val context: Context,
    private val gson: Gson
) : ITaskFileService {

    private val tasksDir = File(context.filesDir, "tasks").apply { mkdirs() }
    private val fileMutex = Mutex()

    // Handler delegation
    private val taskHistoryHandler = TaskHistoryHandler(tasksDir, gson, fileMutex)
    private val taskResultHandler = TaskResultHandler(tasksDir, gson, fileMutex)
    private val rawDataHandler = RawDataHandler(context)
    private val fileCleanupService = FileCleanupService(context, tasksDir)

    // Task History operations
    override suspend fun saveTaskHistory(profileId: String, tasks: List<Task>): Boolean = 
        taskHistoryHandler.save(profileId, tasks)

    override suspend fun loadTaskHistory(profileId: String): TaskHistory? = 
        taskHistoryHandler.load(profileId)

    override suspend fun loadAllTaskHistories(): Map<String, TaskHistory> = 
        taskHistoryHandler.loadAll()

    override suspend fun deleteTaskHistoryFile(profileId: String): Boolean = 
        taskHistoryHandler.delete(profileId)

    // Task Result operations
    override suspend fun saveTaskResult(result: TaskResult): Boolean = 
        taskResultHandler.save(result)

    override suspend fun loadTaskResult(taskId: String): TaskResult? = 
        taskResultHandler.load(taskId)

    override suspend fun deleteTaskFiles(taskId: String): Boolean = 
        taskResultHandler.delete(taskId)

    // Raw Data operations
    override suspend fun saveRawDataToFile(taskId: String, data: String): String? = 
        rawDataHandler.save(taskId, data)

    override suspend fun loadRawDataFromFile(filePath: String): String? = 
        rawDataHandler.load(filePath)

    // Cleanup operations
    override suspend fun cleanupOldFiles(daysToKeep: Int): Boolean = 
        fileCleanupService.cleanupOldFiles(daysToKeep)
}