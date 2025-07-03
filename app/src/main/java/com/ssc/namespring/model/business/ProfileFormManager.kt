// model/business/ProfileFormManager.kt
package com.ssc.namespring.model.business

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.data.CharInfo
import com.ssc.namespring.model.data.GivenNameInfo
import com.ssc.namespring.model.repository.HanjaSearchResult
import com.ssc.namespring.model.repository.NameData
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.repository.ProfileManager
import com.ssc.namespring.model.repository.SurnameData
import com.ssc.namespring.model.data.SurnameInfo
import com.ssc.namespring.model.repository.SurnameSearchResult
import java.text.Normalizer
import java.util.Calendar

class ProfileFormViewModel(private val profileId: String? = null) {
    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState

    val selectedDate: Calendar = Calendar.getInstance()
    private var selectedSurname: SurnameInfo? = null
    private val nameCharDataList = mutableListOf<NameCharData>()
    private val selectedHanjaInfo = mutableMapOf<Int, NameData.CharTripleInfo>()

    init {
        _uiState.value = ProfileFormUiState()
    }

    fun initialize() {
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())

        profileId?.let { id ->
            ProfileManager.getProfile(id)?.let { profile ->
                loadProfileData(profile)
            }
        } ?: run {
            updateUiState()
        }
    }

    private fun loadProfileData(profile: Profile) {
        selectedDate.time = profile.birthDate.time
        selectedSurname = profile.surname

        nameCharDataList.clear()
        profile.givenName?.let { givenName ->
            givenName.charInfos.forEach { charInfo ->
                nameCharDataList.add(NameCharData(
                    korean = charInfo.korean,
                    hanja = charInfo.hanja
                ))
            }

            // 한자 정보 복원
            givenName.charInfos.forEachIndexed { index, charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    NameData.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                        selectedHanjaInfo[index] = info
                    }
                }
            }
        }

        if (nameCharDataList.isEmpty()) {
            nameCharDataList.add(NameCharData())
        }

        _uiState.value = ProfileFormUiState(
            profileName = profile.profileName,
            birthDateText = formatDate(selectedDate),
            birthTimeText = formatTime(selectedDate),
            isYajaTime = profile.isYajaTime,
            selectedSurname = selectedSurname,
            nameCharCount = nameCharDataList.size,
            nameCharDataList = nameCharDataList.toList()
        )
    }

    fun updateDate(calendar: Calendar) {
        selectedDate.time = calendar.time
        updateUiState()
    }

    fun updateTime(calendar: Calendar) {
        selectedDate.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        selectedDate.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        updateUiState()
    }

    fun updateYajaTime(isChecked: Boolean) {
        _uiState.value = _uiState.value?.copy(isYajaTime = isChecked)
    }

    fun addNameChar() {
        val currentCount = _uiState.value?.nameCharCount ?: 1

        if (currentCount < 4) {
            // nameCharDataList에 이미 데이터가 있으면 복원, 없으면 새로 추가
            if (nameCharDataList.size <= currentCount) {
                nameCharDataList.add(NameCharData())
            }

            val newCount = currentCount + 1

            _uiState.value = _uiState.value?.copy(
                nameCharCount = newCount,
                nameCharDataList = nameCharDataList.take(newCount) // 표시할 데이터만 전달
            )
        }
    }

    fun removeNameChar() {
        if (nameCharDataList.size > 1) {
            // nameCharCount를 줄이되, 데이터는 리스트에 유지
            // 이렇게 하면 나중에 + 버튼을 눌렀을 때 데이터 복원 가능
            val newCount = nameCharDataList.size - 1

            // UI 상태 업데이트 - nameCharCount만 줄임
            _uiState.value = _uiState.value?.copy(
                nameCharCount = newCount,
                nameCharDataList = nameCharDataList.take(newCount) // 표시할 데이터만 전달
            )
        }
    }

    fun showSurnameDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_surname_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = SurnameSearchAdapter { result ->
            selectedSurname = SurnameData.getSurnameInfo(result.korean, result.hanja)
            updateUiState()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(context)
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
                val query = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    val results = SurnameData.searchSurnames(query)
                    adapter.submitList(results)
                } else {
                    adapter.submitList(emptyList())
                }
            }
        })

        dialog.show()
    }

    fun showHanjaSearchDialog(context: Context, position: Int) {
        val initialQuery = if (position < nameCharDataList.size) {
            nameCharDataList[position].korean
        } else ""

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hanja_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

        if (initialQuery.isNotEmpty()) {
            etSearch.setText(initialQuery)
        }

        val adapter = HanjaSearchAdapter { result ->
            NameData.getCharInfo(result.tripleKey)?.let { info ->
                selectedHanjaInfo[position] = info
            }

            if (position < nameCharDataList.size) {
                nameCharDataList[position].korean = result.korean
                nameCharDataList[position].hanja = result.hanja
                updateUiState()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(context)
            .setTitle("한자 선택")
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
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    val results = NameData.searchHanja(query)
                    adapter.submitList(results)
                } else {
                    adapter.submitList(emptyList())
                }
            }
        })

        if (initialQuery.isNotEmpty()) {
            val results = NameData.searchHanja(initialQuery)
            adapter.submitList(results)
        }

        dialog.show()
    }

    fun createNameInputView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        index: Int,
        data: NameCharData,
        onSearchClick: (Int) -> Unit
    ): View {
        val view = inflater.inflate(R.layout.item_name_input, parent, false)

        val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
        val etKorean = view.findViewById<EditText>(R.id.etKorean)
        val etHanja = view.findViewById<EditText>(R.id.etHanja)
        val btnSearchHanja = view.findViewById<Button>(R.id.btnSearchHanja)
        val btnClearChar = view.findViewById<ImageButton>(R.id.btnClearChar)

        val positions = arrayOf("첫째", "둘째", "셋째", "넷째")
        tvPosition.text = positions[index]

        etKorean.setText(data.korean)
        etHanja.setText(data.hanja)
        updateButtonText(btnSearchHanja, data.korean, data.hanja)

        etKorean.addTextChangedListener(KoreanTextWatcher(index, etHanja, btnSearchHanja))
        etHanja.addTextChangedListener(HanjaTextWatcher(index, etKorean, btnSearchHanja))

        btnSearchHanja.setOnClickListener { onSearchClick(index) }
        btnClearChar.setOnClickListener {
            etKorean.setText("")
            etHanja.setText("")
            selectedHanjaInfo.remove(index)
            if (index < nameCharDataList.size) {
                nameCharDataList[index] = NameCharData()
                updateUiState()
            }
        }

        return view
    }

    fun showResetDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("전체 초기화")
            .setMessage("입력한 모든 정보가 초기화됩니다. 계속하시겠습니까?")
            .setPositiveButton("초기화") { _, _ ->
                resetAllFields()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun resetAllFields() {
        selectedDate.time = Calendar.getInstance().time
        selectedSurname = null
        selectedHanjaInfo.clear()
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())

        _uiState.value = ProfileFormUiState()
    }

    fun saveProfile(context: Context, profileName: String, callback: (Boolean) -> Unit) {
        val givenName = getGivenNameInfo()

        val profile = profileId?.let { id ->
            ProfileManager.getProfile(id)?.copy(
                profileName = profileName,
                birthDate = selectedDate,
                isYajaTime = _uiState.value?.isYajaTime ?: false,
                surname = selectedSurname,
                givenName = givenName,
                updatedAt = System.currentTimeMillis()
            )
        } ?: Profile(
            profileName = profileName,
            birthDate = selectedDate,
            isYajaTime = _uiState.value?.isYajaTime ?: false,
            surname = selectedSurname,
            givenName = givenName
        )

        val success = if (profile != null) {
            if (profileId != null) {
                ProfileManager.updateProfile(profile)
            } else {
                ProfileManager.addProfile(profile)
            }
        } else {
            false
        }

        if (!success) {
            AlertDialog.Builder(context)
                .setTitle("중복된 프로필")
                .setMessage("동일한 프로필이 이미 존재합니다.\n(프로필명, 생년월일시분, 성씨가 모두 동일)")
                .setPositiveButton("확인", null)
                .show()
        }

        callback(success)
    }

    private fun getGivenNameInfo(): GivenNameInfo? {
        val charInfos = mutableListOf<CharInfo>()

        for (i in 0 until nameCharDataList.size) {
            val data = nameCharDataList[i]

            if (data.korean.isNotEmpty() || data.hanja.isNotEmpty()) {
                val info = selectedHanjaInfo[i]
                charInfos.add(
                    CharInfo(
                        korean = data.korean,
                        hanja = data.hanja,
                        meaning = info?.integratedInfo?.nameMeaning,
                        strokes = info?.hanjaInfo?.strokes ?: 0,
                        ohaeng = info?.hanjaInfo?.ohaeng,
                        eumyang = info?.hanjaInfo?.eumyang ?: 0
                    )
                )
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

    private fun updateUiState() {
        _uiState.value = _uiState.value?.copy(
            birthDateText = formatDate(selectedDate),
            birthTimeText = formatTime(selectedDate),
            selectedSurname = selectedSurname,
            nameCharCount = nameCharDataList.size,
            nameCharDataList = nameCharDataList.toList()
        ) ?: ProfileFormUiState(
            profileName = "",
            birthDateText = formatDate(selectedDate),
            birthTimeText = formatTime(selectedDate),
            isYajaTime = true,
            selectedSurname = selectedSurname,
            nameCharCount = nameCharDataList.size,
            nameCharDataList = nameCharDataList.toList()
        )
    }

    private fun formatDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%d년 %d월 %d일", year, month, day)
    }

    private fun formatTime(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d시 %02d분", hour, minute)
    }

    private fun updateButtonText(button: Button, korean: String, hanja: String) {
        when {
            hanja.isNotEmpty() -> {
                button.text = "한자 변경"
                button.setTextColor(button.context.getColor(R.color.white))
            }
            korean.length == 1 && korean.matches(Regex("[가-힣]")) -> {
                val results = NameData.searchHanja(korean)
                button.text = if (results.isNotEmpty()) {
                    "예시: ${results[0].hanja}"
                } else {
                    "한자 검색"
                }
                button.setTextColor(button.context.getColor(R.color.white))
            }
            else -> {
                button.text = "한자 검색"
                button.setTextColor(button.context.getColor(R.color.white))
            }
        }
    }

    inner class KoreanTextWatcher(
        private val index: Int,
        private val etHanja: EditText,
        private val btnSearchHanja: Button
    ) : TextWatcher {
        private var previousText = ""
        private var isInternalChange = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            previousText = s?.toString() ?: ""
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isInternalChange) return

            val text = s?.toString() ?: ""
            val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)

            if (text != normalized) {
                isInternalChange = true
                s?.replace(0, s.length, normalized)
                isInternalChange = false
                return
            }

            if (normalized.isNotEmpty() && !normalized.matches(Regex("^[가-힣ㄱ-ㅎㅏ-ㅣ]$"))) {
                isInternalChange = true
                s?.replace(0, s.length, previousText)
                isInternalChange = false
                Toast.makeText(etHanja.context, "한글만 입력 가능합니다", Toast.LENGTH_SHORT).show()
                return
            }

            if (previousText != normalized && etHanja.text.isNotEmpty()) {
                etHanja.setText("")
                selectedHanjaInfo.remove(index)
                if (index < nameCharDataList.size) {
                    nameCharDataList[index].hanja = ""
                }
                Toast.makeText(etHanja.context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
            }

            if (index < nameCharDataList.size) {
                nameCharDataList[index].korean = normalized
            }

            updateButtonText(btnSearchHanja, normalized, etHanja.text.toString())
        }
    }

    inner class HanjaTextWatcher(
        private val index: Int,
        private val etKorean: EditText,
        private val btnSearchHanja: Button
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val text = s?.toString() ?: ""
            if (index < nameCharDataList.size) {
                nameCharDataList[index].hanja = text
            }
            updateButtonText(btnSearchHanja, etKorean.text.toString(), text)
        }
    }
}

data class ProfileFormUiState(
    val profileName: String = "",
    val birthDateText: String = "",
    val birthTimeText: String = "",
    val isYajaTime: Boolean = true,
    val selectedSurname: SurnameInfo? = null,
    val nameCharCount: Int = 1,
    val nameCharDataList: List<NameCharData> = listOf(NameCharData())
)

data class NameCharData(
    var korean: String = "",
    var hanja: String = ""
)

// Adapters
class HanjaSearchAdapter(
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

            val ohaengColor = when(result.ohaeng) {
                "木" -> R.color.ohaeng_wood
                "火" -> R.color.ohaeng_fire
                "土" -> R.color.ohaeng_earth
                "金" -> R.color.ohaeng_metal
                "水" -> R.color.ohaeng_water
                else -> R.color.text_secondary
            }
            tvOhaeng.setTextColor(itemView.context.getColor(ohaengColor))

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

class SurnameSearchAdapter(
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

            if (result.isCompound) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.sunny_spring_bg))
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.white))
            }

            itemView.setOnClickListener {
                onItemClick(result)
                onItemSelected?.invoke()
            }
        }
    }
}
