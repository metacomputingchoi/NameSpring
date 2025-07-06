// ui/profileform/ProfileFormUIComponents.kt
package com.ssc.namespring.ui.profileform

import android.app.Activity
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode

class ProfileFormUIComponents(
    private val activity: Activity,
    private val config: ProfileFormConfig
) {
    val etProfileName: TextInputEditText = activity.findViewById(R.id.etProfileName)
    val profileNameLayout: TextInputLayout = activity.findViewById(R.id.profileNameLayout)
    val tvBirthDate: TextView = activity.findViewById(R.id.tvBirthDate)
    val tvBirthTime: TextView = activity.findViewById(R.id.tvBirthTime)
    val btnSelectDate: Button = activity.findViewById(R.id.btnSelectDate)
    val btnSelectTime: Button = activity.findViewById(R.id.btnSelectTime)
    val cbYajaTime: CheckBox = activity.findViewById(R.id.cbYajaTime)
    val btnSelectSurname: Button = activity.findViewById(R.id.btnSelectSurname)
    val tvSelectedSurname: TextView = activity.findViewById(R.id.tvSelectedSurname)
    val btnSave: Button = activity.findViewById(R.id.btnSave)
    val btnReset: Button = activity.findViewById(R.id.btnReset)
    val tvNameInputLabel: TextView = activity.findViewById(R.id.tvNameInputLabel)
    val scrollView: ScrollView? = activity.findViewById(R.id.scrollView)

    // btnLoadProfile의 실제 타입에 따라 수정
    val btnLoadProfile: View? = try {
        // 먼저 Button으로 시도
        activity.findViewById<Button?>(R.id.btnLoadProfile)
    } catch (e: ClassCastException) {
        // Button이 아니면 ImageButton으로 시도
        try {
            activity.findViewById<ImageButton?>(R.id.btnLoadProfile)
        } catch (e2: Exception) {
            // 그것도 아니면 일반 View로
            activity.findViewById<View?>(R.id.btnLoadProfile)
        }
    }

    val nameInputContainer: LinearLayout = activity.findViewById(R.id.nameInputContainer)
    val btnAddChar: ImageButton = activity.findViewById(R.id.btnAddChar)
    val btnRemoveChar: ImageButton = activity.findViewById(R.id.btnRemoveChar)
    val tvCharCount: TextView = activity.findViewById(R.id.tvCharCount)
    val btnBack: ImageButton = activity.findViewById(R.id.btnBack)
    val tvTitle: TextView = activity.findViewById(R.id.tvTitle)

    init {
        setupUIFromConfig()
        initializeTextInputLayout()
    }

    private fun setupUIFromConfig() {
        // 타이틀 설정
        tvTitle.text = config.titleText

        // 프로필 이름 라벨 설정 - hint 대신 다른 텍스트 사용
        profileNameLayout.hint = "" // 빈 문자열로 설정하거나 제거

        // 이름 입력 레이블 설정 (추가)
        tvNameInputLabel.text = config.nameInputLabelText

        // 기본값 설정 (작명/평가 모드)
        if (config.mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)) {
            etProfileName.setText(config.getDefaultName())
            // 야자시 기본 체크
            cbYajaTime.isChecked = true
        }

        // 로드 버튼 표시 여부
        btnLoadProfile?.visibility = if (config.showLoadButton) View.VISIBLE else View.GONE

        // 힌트 설정
        etProfileName.hint = config.profileLabelHint

        // 저장 버튼 텍스트 설정
        btnSave.text = config.saveButtonText

        val profileLabel = activity.findViewById<TextView>(R.id.tvProfileLabel)
        profileLabel?.text = config.profileLabelText
    }

    private fun initializeTextInputLayout() {
        // TextInputLayout의 hint를 처음부터 위쪽에 표시하기 위한 트릭
        profileNameLayout.isHintAnimationEnabled = true

        // 초기에 공백 문자를 넣었다가 제거하여 hint를 위로 올림
        if (etProfileName.text.isNullOrEmpty()) {
            etProfileName.setText(" ")
            etProfileName.post {
                etProfileName.setText("")
            }
        }
    }
}