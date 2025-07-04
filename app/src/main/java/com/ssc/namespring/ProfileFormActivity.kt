// ProfileFormActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.model.business.*
import com.ssc.namespring.model.repository.NameData
import com.ssc.namespring.utils.ViewUtils

class ProfileFormActivity : AppCompatActivity() {
    private lateinit var formManager: ProfileFormManager
    private lateinit var searchDialogManager: SearchDialogManager
    private lateinit var profileFormService: ProfileFormService
    private var nameInputManager: NameInputManager? = null

    // UI Components
    private lateinit var etProfileName: TextInputEditText
    private lateinit var profileNameLayout: TextInputLayout
    private lateinit var tvBirthDate: TextView
    private lateinit var tvBirthTime: TextView
    private lateinit var btnSelectDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var cbYajaTime: CheckBox
    private lateinit var btnSelectSurname: Button
    private lateinit var tvSelectedSurname: TextView
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var nameInputContainer: LinearLayout
    private lateinit var btnAddChar: ImageButton
    private lateinit var btnRemoveChar: ImageButton
    private lateinit var tvCharCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        val profileId = intent.getStringExtra("profileId")
        formManager = ProfileFormManager(profileId)
        searchDialogManager = SearchDialogManager()
        profileFormService = ProfileFormService()

        initViews()
        observeFormState()
        formManager.initialize()
    }

    private fun initViews() {
        etProfileName = findViewById(R.id.etProfileName)
        profileNameLayout = findViewById(R.id.profileNameLayout)
        tvBirthDate = findViewById(R.id.tvBirthDate)
        tvBirthTime = findViewById(R.id.tvBirthTime)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSelectTime = findViewById(R.id.btnSelectTime)
        cbYajaTime = findViewById(R.id.cbYajaTime)
        btnSelectSurname = findViewById(R.id.btnSelectSurname)
        tvSelectedSurname = findViewById(R.id.tvSelectedSurname)
        btnSave = findViewById(R.id.btnSave)
        btnReset = findViewById(R.id.btnReset)
        nameInputContainer = findViewById(R.id.nameInputContainer)
        btnAddChar = findViewById(R.id.btnAddChar)
        btnRemoveChar = findViewById(R.id.btnRemoveChar)
        tvCharCount = findViewById(R.id.tvCharCount)

        setupListeners()
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnSelectDate.setOnClickListener {
            ViewUtils.showDatePicker(this, formManager.getSelectedDate()) { date ->
                formManager.updateDate(date)
            }
        }

        btnSelectTime.setOnClickListener {
            ViewUtils.showTimePicker(this, formManager.getSelectedDate()) { time ->
                formManager.updateTime(time)
            }
        }

        btnSelectSurname.setOnClickListener {
            searchDialogManager.showSurnameDialog(this) { surname ->
                formManager.setSurname(surname)
            }
        }

        btnAddChar.setOnClickListener { formManager.addNameChar() }
        btnRemoveChar.setOnClickListener { formManager.removeNameChar() }

        btnSave.setOnClickListener { saveProfile() }
        btnReset.setOnClickListener {
            profileFormService.showResetDialog(this) {
                formManager.resetAllFields()
            }
        }

        cbYajaTime.setOnCheckedChangeListener { _, isChecked ->
            formManager.updateYajaTime(isChecked)
        }

        ViewUtils.setupProfileNameInput(etProfileName, profileNameLayout)
    }

    private fun observeFormState() {
        formManager.uiState.observe(this) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: ProfileFormUiState) {
        // 프로필 이름은 사용자가 직접 변경한 경우에만 업데이트
        if (etProfileName.text.toString() != state.profileName && state.profileName.isNotEmpty()) {
            etProfileName.setText(state.profileName)
        }

        tvBirthDate.text = state.birthDateText
        tvBirthTime.text = state.birthTimeText
        cbYajaTime.isChecked = state.isYajaTime

        if (state.selectedSurname != null) {
            tvSelectedSurname.text = "${state.selectedSurname.korean}(${state.selectedSurname.hanja})"
            tvSelectedSurname.visibility = android.view.View.VISIBLE
        } else {
            tvSelectedSurname.visibility = android.view.View.GONE
        }

        tvCharCount.text = "${state.nameCharCount}글자"
        btnAddChar.isEnabled = state.nameCharCount < 4
        btnRemoveChar.isEnabled = state.nameCharCount > 1

        refreshNameInputViews(state)
    }

    private fun refreshNameInputViews(state: ProfileFormUiState) {
        nameInputContainer.removeAllViews()

        // NameInputManager를 여기서 초기화
        if (nameInputManager == null) {
            nameInputManager = NameInputManager(
                formManager.getNameDataManager()
            ) { position ->
                handleHanjaSearch(position)
            }
        }

        state.nameCharDataList.forEachIndexed { index, _ ->
            nameInputManager?.let { manager ->
                val itemView = manager.createNameInputView(
                    this,
                    layoutInflater,
                    nameInputContainer,
                    index
                )
                nameInputContainer.addView(itemView)
            }
        }
    }

    private fun handleHanjaSearch(position: Int) {
        val currentData = formManager.getNameDataManager().getCharData(position)
        val initialQuery = currentData?.korean ?: ""

        searchDialogManager.showHanjaSearchDialog(
            this,
            position,
            initialQuery
        ) { pos, korean, hanja ->
            formManager.setHanjaInfo(pos, korean, hanja)

            // 한자 정보 저장
            NameData.getCharInfo(korean, hanja)?.let { info ->
                formManager.getNameDataManager().setHanjaInfo(pos, info)
            }
        }
    }

    private fun saveProfile() {
        val profileName = etProfileName.text?.toString() ?: ""

        if (profileName.isEmpty()) {
            Toast.makeText(this, "프로필 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val profileId = intent.getStringExtra("profileId")

        profileFormService.saveProfile(
            this,
            formManager,
            profileName,
            profileId
        ) { success ->
            if (success) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nameInputManager = null
    }
}