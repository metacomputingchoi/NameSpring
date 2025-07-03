// ProfileFormActivity.kt
package com.ssc.namespring

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.model.*
import java.text.Normalizer
import java.util.Calendar

class ProfileFormActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileFormActivity"
    }

    // UI 컴포넌트
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

    // 이름 입력 관련
    private lateinit var nameInputContainer: LinearLayout
    private lateinit var btnAddChar: ImageButton
    private lateinit var btnRemoveChar: ImageButton
    private lateinit var tvCharCount: TextView

    // 데이터
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedSurname: SurnameInfo? = null
    private var profileToEdit: Profile? = null
    private var nameCharCount = 1
    private val selectedHanjaInfo = mutableMapOf<Int, NameData.CharTripleInfo>()
    private val nameCharDataList = mutableListOf<NameCharData>()

    // 이름 글자 데이터 클래스
    data class NameCharData(
        var korean: String = "",
        var hanja: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        Log.d(TAG, "onCreate started")

        try {
            setupViews()
            setupInitialData()

            // 편집 모드 체크
            intent.getStringExtra("profileId")?.let { id ->
                Log.d(TAG, "Edit mode - profileId: $id")
                ProfileManager.getProfile(id)?.let { profile ->
                    profileToEdit = profile
                    loadProfileData(profile)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error", e)
            Toast.makeText(this, "초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViews() {
        Log.d(TAG, "setupViews started")

        // 뷰 초기화
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

        // 프로필 이름 입력 완료 처리
        etProfileName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etProfileName.clearFocus()
                profileNameLayout.isEndIconVisible = true
                true
            } else {
                false
            }
        }

        // 뒤로가기 버튼
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 전체 초기화 버튼
        btnReset.setOnClickListener {
            showResetConfirmDialog()
        }

        // 날짜 선택
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // 시간 선택
        btnSelectTime.setOnClickListener {
            showTimePicker()
        }

        // 성씨 선택
        btnSelectSurname.setOnClickListener {
            showSurnameDialog()
        }

        // 저장
        btnSave.setOnClickListener {
            saveProfile()
        }

        // + 버튼 (최대 4글자)
        btnAddChar.setOnClickListener {
            if (nameCharCount < 4) {
                nameCharCount++
                while (nameCharDataList.size < nameCharCount) {
                    nameCharDataList.add(NameCharData())
                }
                refreshNameInputViews()
            }
        }

        // - 버튼 (최소 1글자)
        btnRemoveChar.setOnClickListener {
            if (nameCharCount > 1) {
                selectedHanjaInfo.remove(nameCharCount - 1)
                nameCharCount--
                refreshNameInputViews()
            }
        }

        updateDateTimeDisplay()
    }

    private fun setupInitialData() {
        Log.d(TAG, "setupInitialData")
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())
        nameCharCount = 1
        refreshNameInputViews()
    }

    private fun refreshNameInputViews() {
        Log.d(TAG, "refreshNameInputViews - nameCharCount: $nameCharCount")

        // 컨테이너 초기화
        nameInputContainer.removeAllViews()

        // 새로운 입력 필드 생성
        val positions = arrayOf("첫째", "둘째", "셋째", "넷째")

        for (i in 0 until nameCharCount) {
            val itemView = createNameInputItemView(i, positions[i])
            nameInputContainer.addView(itemView)
        }

        // UI 업데이트
        tvCharCount.text = "${nameCharCount}글자"
        btnAddChar.isEnabled = nameCharCount < 4
        btnRemoveChar.isEnabled = nameCharCount > 1
    }

    private fun createNameInputItemView(index: Int, position: String): View {
        val view = layoutInflater.inflate(R.layout.item_name_input, nameInputContainer, false)

        val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
        val etKorean = view.findViewById<EditText>(R.id.etKorean)
        val etHanja = view.findViewById<EditText>(R.id.etHanja)
        val btnSearchHanja = view.findViewById<Button>(R.id.btnSearchHanja)
        val btnClearChar = view.findViewById<ImageButton>(R.id.btnClearChar)

        tvPosition.text = position

        // 데이터 복원
        if (index < nameCharDataList.size) {
            val data = nameCharDataList[index]
            Log.d(TAG, "Restoring data for index $index: korean='${data.korean}', hanja='${data.hanja}'")
            etKorean.setText(data.korean)
            etHanja.setText(data.hanja)
            updateButtonText(btnSearchHanja, data.korean, data.hanja)
        }

        // 한글 입력 리스너
        etKorean.addTextChangedListener(object : TextWatcher {
            private var previousText = ""
            private var isInternalChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isInternalChange) return

                val text = s?.toString() ?: ""

                // 유니코드 정규화 (NFC)
                val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)

                if (text != normalized) {
                    isInternalChange = true
                    etKorean.setText(normalized)
                    etKorean.setSelection(normalized.length)
                    isInternalChange = false
                    return
                }

                // 한글 검증
                if (normalized.isNotEmpty() && !normalized.matches(Regex("^[가-힣ㄱ-ㅎㅏ-ㅣ]$"))) {
                    isInternalChange = true
                    etKorean.setText(previousText)
                    etKorean.setSelection(previousText.length)
                    isInternalChange = false
                    Toast.makeText(this@ProfileFormActivity,
                        "$position 한글란에는 한글만 입력 가능합니다",
                        Toast.LENGTH_SHORT).show()
                    return
                }

                // 한글이 변경되었을 때 한자 초기화
                if (previousText != normalized && etHanja.text.isNotEmpty()) {
                    etHanja.setText("")
                    selectedHanjaInfo.remove(index)
                    if (index < nameCharDataList.size) {
                        nameCharDataList[index].hanja = ""
                    }
                    Toast.makeText(this@ProfileFormActivity,
                        "한글이 변경되어 한자가 초기화되었습니다",
                        Toast.LENGTH_SHORT).show()
                }

                // 데이터 저장
                if (index < nameCharDataList.size) {
                    nameCharDataList[index].korean = normalized
                }

                // 버튼 텍스트 업데이트
                updateButtonText(btnSearchHanja, normalized, etHanja.text.toString())
            }
        })

        // 한자 입력 리스너
        etHanja.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""

                // 데이터 저장
                if (index < nameCharDataList.size) {
                    nameCharDataList[index].hanja = text
                }

                // 버튼 텍스트 업데이트
                updateButtonText(btnSearchHanja, etKorean.text.toString(), text)
            }
        })

        // 한자 검색 버튼
        btnSearchHanja.setOnClickListener {
            val korean = etKorean.text.toString()
            Log.d(TAG, "Search button clicked - index: $index, korean: '$korean'")
            showHanjaSearchDialog(index, korean) { result ->
                etKorean.setText(result.korean)
                etHanja.setText(result.hanja)
            }
        }

        // 초기화 버튼
        btnClearChar.setOnClickListener {
            etKorean.setText("")
            etHanja.setText("")
            selectedHanjaInfo.remove(index)
            if (index < nameCharDataList.size) {
                nameCharDataList[index] = NameCharData()
            }
        }

        return view
    }

    private fun updateButtonText(button: Button, korean: String, hanja: String) {
        when {
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(getColor(R.color.white))
            }
            korean.length == 1 && korean.matches(Regex("[가-힣]")) -> {
                try {
                    val results = NameData.searchHanja(korean)
                    button.text = if (results.isNotEmpty()) {
                        "예시: ${results[0].hanja}"
                    } else {
                        "한자 검색"
                    }
                    button.setTextColor(getColor(R.color.white))
                } catch (e: Exception) {
                    button.text = "한자 검색"
                    button.setTextColor(getColor(R.color.white))
                }
            }
            else -> {
                button.text = "한자 검색"
                button.setTextColor(getColor(R.color.white))
            }
        }
    }

    private fun showHanjaSearchDialog(position: Int, initialQuery: String, onSelected: (HanjaSearchResult) -> Unit) {
        Log.d(TAG, "showHanjaSearchDialog - position: $position, initialQuery: '$initialQuery'")

        val dialogView = layoutInflater.inflate(R.layout.dialog_hanja_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        // 초기값 설정
        if (initialQuery.isNotEmpty()) {
            etSearch.setText(initialQuery)
        }

        val adapter = HanjaSearchAdapter { result ->
            Log.d(TAG, "Hanja selected: ${result.korean}/${result.hanja}")

            // 선택된 한자 정보 저장
            NameData.getCharInfo(result.tripleKey)?.let { info ->
                selectedHanjaInfo[position] = info
            }

            // 콜백 실행
            onSelected(result)

            // 데이터 업데이트
            if (position < nameCharDataList.size) {
                nameCharDataList[position].korean = result.korean
                nameCharDataList[position].hanja = result.hanja
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("한자 선택")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        adapter.onItemSelected = {
            dialog.dismiss()
        }

        // 검색 기능
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""

                Log.d(TAG, "=== 한자 검색 ===")
                Log.d(TAG, "검색어: '$query' (length: ${query.length})")

                // 디버깅: 문자 코드 확인
                if (query.isNotEmpty()) {
                    Log.d(TAG, "문자 코드: ${query.map { "U+${it.code.toString(16).uppercase()}" }}")
                    Log.d(TAG, "초성 여부: ${query.matches(Regex("^[ㄱ-ㅎ]$"))}")
                    Log.d(TAG, "한글 여부: ${query.matches(Regex("^[가-힣]$"))}")
                }

                if (query.isNotEmpty()) {
                    try {
                        val results = NameData.searchHanja(query)
                        Log.d(TAG, "검색 결과: ${results.size}개")
                        adapter.submitList(results)
                    } catch (e: Exception) {
                        Log.e(TAG, "검색 에러", e)
                        adapter.submitList(emptyList())
                    }
                } else {
                    adapter.submitList(emptyList())
                }
            }
        })

        // 초기 검색
        if (initialQuery.isNotEmpty()) {
            try {
                val results = NameData.searchHanja(initialQuery)
                Log.d(TAG, "Initial search results: ${results.size}")
                adapter.submitList(results)
            } catch (e: Exception) {
                Log.e(TAG, "Initial search error", e)
            }
        }

        dialog.show()
    }

    private fun getGivenNameInfo(): GivenNameInfo? {
        val charInfos = mutableListOf<CharInfo>()

        for (i in 0 until nameCharCount) {
            if (i < nameCharDataList.size) {
                val data = nameCharDataList[i]

                if (data.korean.isNotEmpty() || data.hanja.isNotEmpty()) {
                    val info = selectedHanjaInfo[i]
                    charInfos.add(CharInfo(
                        korean = data.korean,
                        hanja = data.hanja,
                        meaning = info?.integratedInfo?.nameMeaning,
                        strokes = info?.hanjaInfo?.strokes ?: 0,
                        ohaeng = info?.hanjaInfo?.ohaeng,
                        eumyang = info?.hanjaInfo?.eumyang ?: 0
                    ))
                }
            }
        }

        return if (charInfos.isNotEmpty()) {
            val koreanString = charInfos.joinToString("") { it.korean }
            val hanjaString = charInfos.joinToString("") { it.hanja }

            GivenNameInfo(
                korean = koreanString,
                hanja = hanjaString,
                charInfos = charInfos
            )
        } else {
            null
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateTimeDisplay() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH) + 1
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        tvBirthDate.text = String.format("%d년 %d월 %d일", year, month, day)
        tvBirthTime.text = String.format("%02d시 %02d분", hour, minute)
    }

    private fun showSurnameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_surname_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = SurnameSearchAdapter { result ->
            selectedSurname = SurnameData.getSurnameInfo(result.korean, result.hanja)
            tvSelectedSurname.text = "${result.korean}(${result.hanja})"
            tvSelectedSurname.visibility = View.VISIBLE
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("성씨 선택")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        adapter.onItemSelected = {
            dialog.dismiss()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    val query = s?.toString() ?: ""
                    if (query.isNotEmpty()) {
                        val results = SurnameData.searchSurnames(query)
                        adapter.submitList(results)
                    } else {
                        adapter.submitList(emptyList())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Surname search error", e)
                }
            }
        })

        dialog.show()
    }

    // saveProfile() 메서드 수정 부분만
    private fun saveProfile() {
        val profileName = etProfileName.text?.toString() ?: ""

        if (profileName.isEmpty()) {
            Toast.makeText(this, "프로필 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSurname == null && profileToEdit?.surname == null) {
            Toast.makeText(this, "성씨를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 이름 유효성 검사
        val validationResult = validateNameInputs()
        if (!validationResult.first) {
            Toast.makeText(this, validationResult.second, Toast.LENGTH_LONG).show()
            return
        }

        val givenName = getGivenNameInfo()

        val profile = profileToEdit?.copy(
            profileName = profileName,
            birthDate = selectedDate,
            isYajaTime = cbYajaTime.isChecked,
            surname = selectedSurname ?: profileToEdit?.surname ?: return,
            givenName = givenName,
            // 임시 사주/오행 정보 생성
            sajuInfo = generateTempSajuInfo(),
            ohaengInfo = generateTempOhaengInfo(),
            nameBomScore = (20..95).random()
        ) ?: Profile(
            profileName = profileName,
            birthDate = selectedDate,
            isYajaTime = cbYajaTime.isChecked,
            surname = selectedSurname ?: return,
            givenName = givenName,
            sajuInfo = generateTempSajuInfo(),
            ohaengInfo = generateTempOhaengInfo(),
            nameBomScore = (20..95).random()
        )

        val success = if (profileToEdit != null) {
            ProfileManager.updateProfile(profile)
        } else {
            ProfileManager.addProfile(profile)
        }

        if (success) {
            finish()
        } else {
            AlertDialog.Builder(this)
                .setTitle("중복된 프로필")
                .setMessage("동일한 프로필이 이미 존재합니다.\n(프로필명, 생년월일시분, 성씨가 모두 동일)")
                .setPositiveButton("확인", null)
                .show()
        }
    }

    // 임시 데이터 생성 메서드 추가
    private fun generateTempSajuInfo(): SajuInfo {
        val pillars = listOf("甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥")
        return SajuInfo(
            yearPillar = pillars.random(),
            monthPillar = pillars.random(),
            dayPillar = pillars.random(),
            hourPillar = pillars.random()
        )
    }

    private fun generateTempOhaengInfo(): OhaengInfo {
        return OhaengInfo(
            wood = (0..5).random(),
            fire = (0..5).random(),
            earth = (0..5).random(),
            metal = (0..5).random(),
            water = (0..5).random()
        )
    }

    private fun validateNameInputs(): Pair<Boolean, String> {
        val positions = arrayOf("첫째", "둘째", "셋째", "넷째")

        for (i in 0 until nameCharCount) {
            if (i < nameCharDataList.size) {
                val data = nameCharDataList[i]

                // 한자 검증 (입력되어 있는 경우만)
                if (data.hanja.isNotEmpty()) {
                    // 데이터베이스에 있는 한자인지 확인
                    val isValidHanja = try {
                        val results = NameData.searchHanja(data.hanja)
                        results.any { it.hanja == data.hanja }
                    } catch (e: Exception) {
                        false
                    }

                    if (!isValidHanja) {
                        return Pair(false, "${positions[i]} 한자란에 유효한 인명용 한자를 입력해주세요")
                    }
                }

                // 한글과 한자가 모두 완성된 글자로 있는 경우만 매칭 확인
                if (data.korean.matches(Regex("^[가-힣]$")) && data.hanja.isNotEmpty()) {
                    val isMatching = try {
                        val charInfo = NameData.getCharInfo(data.korean, data.hanja)
                        charInfo != null
                    } catch (e: Exception) {
                        false
                    }

                    if (!isMatching) {
                        return Pair(false, "${positions[i]}의 한글 '${data.korean}'과 한자 '${data.hanja}'가 서로 매칭되지 않습니다")
                    }
                }
            }
        }

        // 모든 검증 통과
        return Pair(true, "")
    }

    private fun loadProfileData(profile: Profile) {
        Log.d(TAG, "loadProfileData - profile: ${profile.profileName}")

        etProfileName.setText(profile.profileName)
        selectedDate = profile.birthDate.clone() as Calendar
        cbYajaTime.isChecked = profile.isYajaTime

        profile.surname?.let {
            selectedSurname = it
            tvSelectedSurname.text = "${it.korean}(${it.hanja})"
            tvSelectedSurname.visibility = View.VISIBLE
        }

        // 이름 데이터 로드
        nameCharDataList.clear()

        profile.givenName?.let { givenName ->
            Log.d(TAG, "Loading given name: charInfos size = ${givenName.charInfos.size}")

            givenName.charInfos.forEach { charInfo ->
                Log.d(TAG, "Loading char: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
                nameCharDataList.add(NameCharData(
                    korean = charInfo.korean,
                    hanja = charInfo.hanja
                ))
            }

            nameCharCount = givenName.charInfos.size.coerceAtLeast(1)

            // 한자 정보 복원
            givenName.charInfos.forEachIndexed { index, charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    try {
                        NameData.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                            selectedHanjaInfo[index] = info
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restore hanja info", e)
                    }
                }
            }
        }

        if (nameCharDataList.isEmpty()) {
            nameCharDataList.add(NameCharData())
            nameCharCount = 1
        }

        updateDateTimeDisplay()
        refreshNameInputViews()
    }

    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("전체 초기화")
            .setMessage("입력한 모든 정보가 초기화됩니다. 계속하시겠습니까?")
            .setPositiveButton("초기화") { _, _ ->
                resetAllFields()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun resetAllFields() {
        Log.d(TAG, "resetAllFields")

        // 모든 필드 초기화
        etProfileName.setText("")
        selectedDate = Calendar.getInstance()
        updateDateTimeDisplay()
        cbYajaTime.isChecked = false
        selectedSurname = null
        tvSelectedSurname.text = ""
        tvSelectedSurname.visibility = View.GONE

        // 이름 데이터 초기화
        selectedHanjaInfo.clear()
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())
        nameCharCount = 1

        // UI 새로고침
        refreshNameInputViews()

        Toast.makeText(this, "모든 항목이 초기화되었습니다", Toast.LENGTH_SHORT).show()
    }

    // 한자 검색 어댑터
    inner class HanjaSearchAdapter(
        private val onItemClick: (HanjaSearchResult) -> Unit
    ) : RecyclerView.Adapter<HanjaSearchAdapter.ViewHolder>() {

        private var results = listOf<HanjaSearchResult>()
        var onItemSelected: (() -> Unit)? = null

        fun submitList(list: List<HanjaSearchResult>) {
            results = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hanja_search, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(results[position])
        }

        override fun getItemCount() = results.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvHanja: TextView = itemView.findViewById(R.id.tvHanja)
            private val tvKorean: TextView = itemView.findViewById(R.id.tvKorean)
            private val tvMeaning: TextView = itemView.findViewById(R.id.tvMeaning)
            private val tvOhaeng: TextView = itemView.findViewById(R.id.tvOhaeng)
            private val tvStrokes: TextView = itemView.findViewById(R.id.tvStrokes)
            private val tvSoundCount: TextView = itemView.findViewById(R.id.tvSoundCount)

            fun bind(result: HanjaSearchResult) {
                tvHanja.text = result.hanja
                tvKorean.text = result.korean
                tvMeaning.text = result.meaning ?: ""
                tvOhaeng.text = result.ohaeng
                tvStrokes.text = "${result.strokes}획"

                // 오행별 색상
                val ohaengColor = when(result.ohaeng) {
                    "木" -> R.color.ohaeng_wood
                    "火" -> R.color.ohaeng_fire
                    "土" -> R.color.ohaeng_earth
                    "金" -> R.color.ohaeng_metal
                    "水" -> R.color.ohaeng_water
                    else -> R.color.text_secondary
                }
                tvOhaeng.setTextColor(getColor(ohaengColor))

                // 다음(多音) 표시
                if (result.soundCount > 1) {
                    tvSoundCount.visibility = View.VISIBLE
                    tvSoundCount.text = "(다음 ${result.soundCount})"
                } else {
                    tvSoundCount.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    onItemClick(result)
                    onItemSelected?.invoke()
                }
            }
        }
    }

    // 성씨 검색 어댑터
    inner class SurnameSearchAdapter(
        private val onItemClick: (SurnameSearchResult) -> Unit
    ) : RecyclerView.Adapter<SurnameSearchAdapter.ViewHolder>() {

        private var results = listOf<SurnameSearchResult>()
        var onItemSelected: (() -> Unit)? = null

        fun submitList(list: List<SurnameSearchResult>) {
            results = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_surname_search, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(results[position])
        }

        override fun getItemCount() = results.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvKorean: TextView = itemView.findViewById(R.id.tvKorean)
            private val tvHanja: TextView = itemView.findViewById(R.id.tvHanja)
            private val tvMeaning: TextView = itemView.findViewById(R.id.tvMeaning)

            fun bind(result: SurnameSearchResult) {
                tvKorean.text = result.korean
                tvHanja.text = result.hanja
                tvMeaning.text = result.meaning ?: ""

                // 복성인 경우 배경색 변경
                if (result.isCompound) {
                    itemView.setBackgroundColor(getColor(R.color.sunny_spring_bg))
                } else {
                    itemView.setBackgroundColor(getColor(R.color.white))
                }

                itemView.setOnClickListener {
                    onItemClick(result)
                    onItemSelected?.invoke()
                }
            }
        }
    }
}