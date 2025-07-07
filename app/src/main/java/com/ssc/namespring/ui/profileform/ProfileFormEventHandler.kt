// ui/profileform/ProfileFormEventHandler.kt
package com.ssc.namespring.ui.profileform

import android.widget.Toast
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.utils.ui.ViewUtils

class ProfileFormEventHandler(
    private val activity: ProfileFormActivity,
    private val formManager: ProfileFormManager,
    private val searchDialogManager: SearchDialogManager,
    private val profileFormService: ProfileFormService,
    private val uiComponents: ProfileFormUIComponents,
    private val nameInputHandler: ProfileFormNameInputHandler,
    private val config: ProfileFormConfig
) {
    fun setupListeners() {
        uiComponents.btnLoadProfile?.setOnClickListener {
            (activity as? ProfileFormActivity)?.loadParentProfileData()
        }

        uiComponents.btnBack.setOnClickListener { activity.finish() }

        uiComponents.btnSelectDate.setOnClickListener {
            ViewUtils.showDatePicker(activity, formManager.getSelectedDate()) { date ->
                formManager.updateDate(date)
            }
        }

        uiComponents.btnSelectTime.setOnClickListener {
            ViewUtils.showTimePicker(activity, formManager.getSelectedDate()) { time ->
                formManager.updateTime(time)
            }
        }

        uiComponents.btnSelectSurname.setOnClickListener {
            searchDialogManager.showSurnameDialog(activity) { surname ->
                formManager.setSurname(surname)
            }
        }

        uiComponents.btnAddChar.setOnClickListener {
            formManager.addNameChar()
        }

        uiComponents.btnRemoveChar.setOnClickListener {
            formManager.removeNameChar()
        }

        uiComponents.btnSave.setOnClickListener {
            activity.saveProfile()
        }

        uiComponents.btnReset.setOnClickListener {
            profileFormService.showResetDialog(activity) {
                formManager.resetAllFields()
            }
        }

        uiComponents.cbYajaTime.setOnCheckedChangeListener { _, isChecked ->
            formManager.updateYajaTime(isChecked)
        }

        uiComponents.btnLoadProfile?.setOnClickListener {
            loadParentProfileData()
        }

        // 기존 ViewUtils.setupProfileNameInput 호출
        ViewUtils.setupProfileNameInput(
            uiComponents.etProfileName,
            uiComponents.profileNameLayout
        )

        // 프로필 이름 입력 필드 초기화 추가
        setupProfileNameInput()
    }

    private fun setupProfileNameInput() {
        // 초기 포커스를 주어 hint를 위로 올림
        if (uiComponents.etProfileName.text.isNullOrEmpty()) {
            uiComponents.etProfileName.requestFocus()
            uiComponents.etProfileName.post {
                uiComponents.etProfileName.clearFocus()
            }
        }
    }

    private fun loadParentProfileData() {
        config.parentProfileId?.let { parentId ->
            val profileManager = ProfileManagerProvider.getInstance()
            profileManager.getProfile(parentId)?.let { parentProfile ->
                // 프로필 데이터 로드
                formManager.loadFromParentProfile(parentProfile)
                Toast.makeText(activity, "프로필 정보를 불러왔습니다", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(activity, "프로필 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(activity, "부모 프로필 정보가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}