// ui/history/components/namelist/NameListDialogDataManager.kt
package com.ssc.namespring.ui.history.components.namelist

import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.data.NameListDataLoader
import com.ssc.namespring.ui.history.manager.NameSearchManager
import com.ssc.namespring.ui.history.manager.NameSortManager
import com.ssc.namespring.ui.history.viewmodel.NameListViewModel
import com.ssc.namingengine.data.GeneratedName
import java.text.SimpleDateFormat
import java.util.*

class NameListDialogDataManager(
    val taskRepository: TaskRepository,
    val favoriteRepository: FavoriteNameRepository,
    enableFuzzySearch: Boolean = false  // 기본값 false로 오타 허용 비활성화
) {
    val viewModel: NameListViewModel
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
    private val searchHelper = NameListSearchHelper(enableFuzzySearch)

    var birthDateTime: String = ""
        private set
    var birthDateTimeMillis: Long = 0L
        private set

    init {
        viewModel = NameListViewModel(
            taskRepository = taskRepository,
            dataLoader = NameListDataLoader(taskRepository),
            searchManager = NameSearchManager(),
            sortManager = NameSortManager()
        )
    }

    fun loadTask(taskId: String) {
        viewModel.loadTask(taskId)
    }

    fun updateSearchQuery(query: String) {
        viewModel.updateSearchQuery(query)
    }

    fun updateSortOrder(sortOrder: NameSortManager.SortOrder) {
        viewModel.updateSortOrder(sortOrder)
    }

    fun updateBirthInfo(task: Task) {
        task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            val millis = (birthDateTimeStr as? String)?.toLongOrNull()
            millis?.let {
                birthDateTimeMillis = it
                birthDateTime = dateFormat.format(Date(it))
            }
        }
    }

    // 검색 기능을 NameListSearchHelper에 위임
    fun searchNames(names: List<GeneratedName>, query: String): List<GeneratedName> {
        return searchHelper.filterNames(names, query)
    }
}