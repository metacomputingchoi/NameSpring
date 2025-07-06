// model/domain/usecase/NameInputManager.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputUIFactory
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputEventHandler
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputStateManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater

class NameInputManager(
    private val nameDataManager: INameDataManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {
    companion object {
        private const val TAG = "NameInputManager"
    }

    private val stateManager = NameInputStateManager()
    private val eventHandler = NameInputEventHandler(
        nameDataManager,
        stateManager,
        onHanjaSearchClick
    )
    private val uiFactory = NameInputUIFactory(
        eventHandler,
        stateManager
    )

    fun createNameInputView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        index: Int
    ): View {
        val data = nameDataManager.getCharData(index)
        return uiFactory.createNameInputView(context, inflater, parent, index, data)
    }

    fun cleanup() {
        stateManager.cleanup()
        eventHandler.cleanup()
        NameInputButtonUpdater.cleanup()  // 추가
    }
}