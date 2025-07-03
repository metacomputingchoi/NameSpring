// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.model.Profile
import com.ssc.namespring.model.ProfileManager
import java.text.SimpleDateFormat
import java.util.*

class ProfileListActivity : AppCompatActivity() {

    // UI Components
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabSelectAll: ExtendedFloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvProfileCount: TextView
    private lateinit var tvSelectedCount: TextView
    private lateinit var bottomActionBar: LinearLayout
    private lateinit var btnDeleteSelected: Button
    private lateinit var btnCancelSelection: Button
    private lateinit var progressBar: ProgressBar

    // Adapter and Data
    private lateinit var adapter: ProfileAdapter
    private var currentProfiles = listOf<Profile>()
    private var isSelectionMode = false
    private val selectedIds = mutableSetOf<String>()
    private var currentSortType = ProfileManager.SortType.DATE_DESC
    private var currentQuery = ""
    private var currentLayoutType = LayoutType.LIST

    // Pagination
    private var currentPage = 1
    private val pageSize = 20
    private var isLoadingMore = false
    private var hasMoreData = true

    // Activity Result Launchers
    private lateinit var createProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>

    enum class LayoutType {
        LIST, GRID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list_improved)

        setupActivityResultLaunchers()
        setupViews()
        setupRecyclerView()
        setupSearch()
        setupSortChips()
        loadProfiles()
    }

    private fun setupActivityResultLaunchers() {
        createProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // 프로필이 추가되었으므로 리스트 새로고침
                currentPage = 1
                currentProfiles = emptyList()
                loadProfiles()
            }
        }

        editProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // 프로필이 수정되었으므로 리스트 새로고침
                currentPage = 1
                currentProfiles = emptyList()
                loadProfiles()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 항상 프로필 리스트를 새로 로드
        currentPage = 1
        currentProfiles = emptyList()
        loadProfiles()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_mode -> {
                toggleSelectionMode()
                true
            }
            R.id.action_view_type -> {
                toggleLayoutType()
                true
            }
            R.id.action_load_all -> {
                loadAllProfiles()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        fabAdd = findViewById(R.id.fabAdd)
        fabSelectAll = findViewById(R.id.fabSelectAll)
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        tvProfileCount = findViewById(R.id.tvProfileCount)
        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        bottomActionBar = findViewById(R.id.bottomActionBar)
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected)
        btnCancelSelection = findViewById(R.id.btnCancelSelection)
        progressBar = findViewById(R.id.progressBar)

        setSupportActionBar(toolbar)

        // FAB 클릭
        fabAdd.setOnClickListener {
            val intent = Intent(this, ProfileFormActivity::class.java)
            createProfileLauncher.launch(intent)
        }

        // 전체 선택 FAB
        fabSelectAll.setOnClickListener {
            if (selectedIds.size == adapter.getSelectableItemCount()) {
                selectedIds.clear()
            } else {
                selectedIds.clear()
                selectedIds.addAll(adapter.getSelectableIds())
            }
            updateSelectionUI()
            adapter.notifyDataSetChanged()
        }

        // 선택 모드 버튼들
        btnDeleteSelected.setOnClickListener {
            showDeleteConfirmDialog(selectedIds.toList())
        }

        btnCancelSelection.setOnClickListener {
            exitSelectionMode()
        }

        // 빈 화면 버튼
        findViewById<Button>(R.id.btnCreateProfile).setOnClickListener {
            val intent = Intent(this, ProfileFormActivity::class.java)
            createProfileLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProfileAdapter(
            layoutType = currentLayoutType,
            onItemClick = { profile ->
                if (isSelectionMode) {
                    toggleSelection(profile.id)
                } else {
                    ProfileManager.switchProfile(profile.id)
                    startActivity(Intent(this, MainActivity::class.java))
                }
            },
            onItemLongClick = { profile ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    toggleSelection(profile.id)
                }
                true
            },
            onEditClick = { profile ->
                val intent = Intent(this, ProfileFormActivity::class.java)
                intent.putExtra("profileId", profile.id)
                editProfileLauncher.launch(intent)
            },
            onDeleteClick = { profile ->
                showDeleteConfirmDialog(listOf(profile.id))
            }
        )

        recyclerView.adapter = adapter
        updateLayoutManager()

        // 무한 스크롤
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMore && hasMoreData && lastVisibleItem >= totalItemCount - 5) {
                    loadMoreProfiles()
                }
            }
        })
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                currentPage = 1
                loadProfiles()
                return true
            }
        })
    }

    private fun setupSortChips() {
        // 정렬 칩 생성
        val sortOptions = listOf(
            "최신순" to ProfileManager.SortType.DATE_DESC,
            "오래된순" to ProfileManager.SortType.DATE_ASC,
            "점수높은순" to ProfileManager.SortType.SCORE_DESC,
            "점수낮은순" to ProfileManager.SortType.SCORE_ASC,
            "이름순" to ProfileManager.SortType.NAME_ASC,
            "이름역순" to ProfileManager.SortType.NAME_DESC
        )

        sortOptions.forEachIndexed { index, (label, sortType) ->
            val chip = layoutInflater.inflate(R.layout.chip_sort, chipGroup, false) as Chip
            chip.text = label
            chip.isChecked = index == 0
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentSortType = sortType
                    currentPage = 1
                    loadProfiles()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun loadProfiles() {
        progressBar.isVisible = true

        val allProfiles = if (currentQuery.isEmpty()) {
            ProfileManager.getSortedProfiles(currentSortType)
        } else {
            ProfileManager.searchProfiles(currentQuery).let { searchResults ->
                when (currentSortType) {
                    ProfileManager.SortType.NAME_ASC -> searchResults.sortedBy { it.profileName }
                    ProfileManager.SortType.NAME_DESC -> searchResults.sortedByDescending { it.profileName }
                    ProfileManager.SortType.SCORE_DESC -> searchResults.sortedByDescending { it.nameBomScore }
                    ProfileManager.SortType.SCORE_ASC -> searchResults.sortedBy { it.nameBomScore }
                    ProfileManager.SortType.DATE_DESC -> searchResults.sortedByDescending { it.createdAt }
                    ProfileManager.SortType.DATE_ASC -> searchResults.sortedBy { it.createdAt }
                }
            }
        }

        // 디버깅: 프로필 정보 로그
        Log.d("ProfileListActivity", "전체 프로필 수: ${allProfiles.size}")
        allProfiles.forEach { profile ->
            Log.d("ProfileListActivity", "프로필: ${profile.profileName}, " +
                    "오행: 목=${profile.ohaengInfo?.wood}, 화=${profile.ohaengInfo?.fire}, " +
                    "토=${profile.ohaengInfo?.earth}, 금=${profile.ohaengInfo?.metal}, 수=${profile.ohaengInfo?.water}")
        }

        // 페이지네이션
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, allProfiles.size)

        currentProfiles = if (currentPage == 1) {
            allProfiles.subList(0, minOf(pageSize, allProfiles.size))
        } else {
            currentProfiles + allProfiles.subList(startIndex, endIndex)
        }

        hasMoreData = endIndex < allProfiles.size

        adapter.submitList(currentProfiles, isSelectionMode, selectedIds)
        updateUI()

        progressBar.isVisible = false
        isLoadingMore = false
    }

    private fun loadMoreProfiles() {
        if (!isLoadingMore && hasMoreData) {
            isLoadingMore = true
            currentPage++
            loadProfiles()
        }
    }

    private fun loadAllProfiles() {
        progressBar.isVisible = true

        AlertDialog.Builder(this)
            .setTitle("전체 로드")
            .setMessage("모든 프로필을 한 번에 로드하시겠습니까? 프로필이 많을 경우 시간이 걸릴 수 있습니다.")
            .setPositiveButton("로드") { _, _ ->
                currentProfiles = if (currentQuery.isEmpty()) {
                    ProfileManager.getSortedProfiles(currentSortType)
                } else {
                    ProfileManager.searchProfiles(currentQuery)
                }
                hasMoreData = false
                adapter.submitList(currentProfiles, isSelectionMode, selectedIds)
                updateUI()
                progressBar.isVisible = false

                Snackbar.make(recyclerView, "${currentProfiles.size}개의 프로필을 모두 로드했습니다", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소") { _, _ ->
                progressBar.isVisible = false
            }
            .setOnCancelListener {
                progressBar.isVisible = false
            }
            .show()
    }

    private fun toggleSelectionMode() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedIds.clear()
        adapter.setSelectionMode(true)

        // UI 변경
        searchView.isVisible = false
        chipGroup.isVisible = false
        tvSelectedCount.isVisible = true
        bottomActionBar.isVisible = true
        fabAdd.hide()
        fabSelectAll.show()

        updateSelectionUI()
        invalidateOptionsMenu()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        adapter.setSelectionMode(false)

        // UI 복원
        searchView.isVisible = true
        chipGroup.isVisible = true
        tvSelectedCount.isVisible = false
        bottomActionBar.isVisible = false
        fabAdd.show()
        fabSelectAll.hide()

        invalidateOptionsMenu()
    }

    private fun updateSelectionUI() {
        tvSelectedCount.text = "${selectedIds.size}개 선택됨"
        btnDeleteSelected.isEnabled = selectedIds.isNotEmpty()

        // 전체 선택 버튼 텍스트 및 아이콘 업데이트
        if (selectedIds.size == adapter.getSelectableItemCount() && adapter.getSelectableItemCount() > 0) {
            fabSelectAll.text = "전체 해제"
            fabSelectAll.setIconResource(R.drawable.ic_clear)
        } else {
            fabSelectAll.text = "전체 선택"
            fabSelectAll.setIconResource(R.drawable.ic_check)
        }
    }

    private fun toggleSelection(profileId: String) {
        if (selectedIds.contains(profileId)) {
            selectedIds.remove(profileId)
        } else {
            selectedIds.add(profileId)
        }
        updateSelectionUI()
        adapter.notifyItemChanged(currentProfiles.indexOfFirst { it.id == profileId })
    }

    private fun toggleLayoutType() {
        currentLayoutType = if (currentLayoutType == LayoutType.LIST) {
            LayoutType.GRID
        } else {
            LayoutType.LIST
        }

        adapter.setLayoutType(currentLayoutType)
        updateLayoutManager()

        Snackbar.make(recyclerView,
            if (currentLayoutType == LayoutType.LIST) "리스트 보기" else "격자 보기",
            Snackbar.LENGTH_SHORT).show()
    }

    private fun updateLayoutManager() {
        recyclerView.layoutManager = if (currentLayoutType == LayoutType.LIST) {
            LinearLayoutManager(this)
        } else {
            GridLayoutManager(this, 2)
        }
    }

    private fun updateUI() {
        tvProfileCount.text = "총 ${currentProfiles.size}개"

        if (currentProfiles.isEmpty()) {
            recyclerView.isVisible = false
            emptyView.isVisible = true
            fabAdd.hide()
        } else {
            recyclerView.isVisible = true
            emptyView.isVisible = false
            if (!isSelectionMode) {
                fabAdd.show()
            }
        }
    }

    private fun showDeleteConfirmDialog(profileIds: List<String>) {
        val message = if (profileIds.size == 1) {
            val profile = currentProfiles.find { it.id == profileIds[0] }
            "'${profile?.profileName}' 프로필을 삭제하시겠습니까?"
        } else {
            "${profileIds.size}개의 프로필을 삭제하시겠습니까?"
        }

        AlertDialog.Builder(this)
            .setTitle("프로필 삭제")
            .setMessage(message)
            .setPositiveButton("삭제") { _, _ ->
                ProfileManager.deleteProfiles(profileIds)
                if (isSelectionMode) {
                    selectedIds.removeAll(profileIds)
                    updateSelectionUI()
                }
                currentPage = 1
                loadProfiles()

                val snackbarMessage = if (profileIds.size == 1) {
                    "프로필이 삭제되었습니다"
                } else {
                    "${profileIds.size}개의 프로필이 삭제되었습니다"
                }
                Snackbar.make(recyclerView, snackbarMessage, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ProfileAdapter 내부 클래스
    inner class ProfileAdapter(
        private var layoutType: LayoutType,
        private val onItemClick: (Profile) -> Unit,
        private val onItemLongClick: (Profile) -> Boolean,
        private val onEditClick: (Profile) -> Unit,
        private val onDeleteClick: (Profile) -> Unit
    ) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

        private var profiles = listOf<Profile>()
        private var isSelectionMode = false
        private var selectedIds = setOf<String>()

        fun submitList(list: List<Profile>, selectionMode: Boolean = false, selected: Set<String> = setOf()) {
            profiles = list
            isSelectionMode = selectionMode
            selectedIds = selected
            notifyDataSetChanged()
        }

        fun setSelectionMode(enabled: Boolean) {
            isSelectionMode = enabled
            notifyDataSetChanged()
        }

        fun setLayoutType(type: LayoutType) {
            layoutType = type
            notifyDataSetChanged()
        }

        fun getSelectableItemCount() = profiles.size
        fun getSelectableIds() = profiles.map { it.id }

        override fun getItemViewType(position: Int): Int {
            return if (layoutType == LayoutType.LIST) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutId = if (viewType == 0) {
                R.layout.item_profile_improved_list
            } else {
                R.layout.item_profile_improved_grid
            }
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(profiles[position])
        }

        override fun getItemCount() = profiles.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardView: View = itemView.findViewById(R.id.cardView)
            private val checkBox: CheckBox? = itemView.findViewById(R.id.checkBox)
            private val tvProfileName: TextView = itemView.findViewById(R.id.tvProfileName)
            private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
            private val tvBirthDate: TextView = itemView.findViewById(R.id.tvBirthDate)
            private val tvBirthTime: TextView = itemView.findViewById(R.id.tvBirthTime)
            private val tvSaju: TextView = itemView.findViewById(R.id.tvSaju)
            private val tvOhaeng: TextView = itemView.findViewById(R.id.tvOhaeng)
            private val ivSprout: ImageView = itemView.findViewById(R.id.ivSprout)
            private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
            private val btnMenu: ImageButton? = itemView.findViewById(R.id.btnMenu)
            private val scoreContainer: View = itemView.findViewById(R.id.scoreContainer)

            fun bind(profile: Profile) {
                // 기본 정보
                tvProfileName.text = profile.profileName
                tvFullName.text = formatFullName(profile)

                // 날짜 포맷 간소화
                val cal = profile.birthDate
                tvBirthDate.text = String.format("%d.%02d.%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH))

                // 시간 포맷
                tvBirthTime.text = String.format("%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE))

                // 사주 정보 표시 (있을 경우만)
                profile.sajuInfo?.let { saju ->
                    tvSaju.text = saju.fourPillars.joinToString(" ")
                    tvSaju.visibility = View.VISIBLE
                } ?: run {
                    tvSaju.visibility = View.GONE
                }

                // 오행 분포 표시 (추가된 부분)
                val tvWoodDist: TextView? = itemView.findViewById(R.id.tvWoodDist)
                val tvFireDist: TextView? = itemView.findViewById(R.id.tvFireDist)
                val tvEarthDist: TextView? = itemView.findViewById(R.id.tvEarthDist)
                val tvMetalDist: TextView? = itemView.findViewById(R.id.tvMetalDist)
                val tvWaterDist: TextView? = itemView.findViewById(R.id.tvWaterDist)
                val ohaengDistribution: LinearLayout? = itemView.findViewById(R.id.ohaengDistribution)

                profile.ohaengInfo?.let { ohaeng ->
                    // 오행 분포 표시
                    tvWoodDist?.text = "木${ohaeng.wood}"
                    tvFireDist?.text = "火${ohaeng.fire}"
                    tvEarthDist?.text = "土${ohaeng.earth}"
                    tvMetalDist?.text = "金${ohaeng.metal}"
                    tvWaterDist?.text = "水${ohaeng.water}"
                    ohaengDistribution?.visibility = View.VISIBLE

                    // 부족/과다 오행 표시
                    val lacking = ohaeng.getLackingOhaeng()
                    val excessive = ohaeng.getExcessOhaeng()

                    val ohaengText = when {
                        lacking.isNotEmpty() && excessive.isNotEmpty() ->
                            "부족: ${lacking.joinToString(",")} | 과다: ${excessive.joinToString(",")}"
                        lacking.isNotEmpty() ->
                            "부족한 오행: ${lacking.joinToString(", ")}"
                        excessive.isNotEmpty() ->
                            "과다한 오행: ${excessive.joinToString(", ")}"
                        else -> "오행 균형"
                    }

                    tvOhaeng.text = ohaengText
                    tvOhaeng.visibility = View.VISIBLE

                    // 부족한 오행의 색상 적용
                    if (lacking.isNotEmpty()) {
                        val color = when(lacking.first()) {
                            "목" -> R.color.ohaeng_wood
                            "화" -> R.color.ohaeng_fire
                            "토" -> R.color.ohaeng_earth
                            "금" -> R.color.ohaeng_metal
                            "수" -> R.color.ohaeng_water
                            else -> R.color.text_secondary
                        }
                        tvOhaeng.setTextColor(itemView.context.getColor(color))
                    }
                } ?: run {
                    ohaengDistribution?.visibility = View.GONE
                    tvOhaeng.text = "오행 정보 없음"
                    tvOhaeng.visibility = View.VISIBLE
                    tvOhaeng.setTextColor(itemView.context.getColor(R.color.text_secondary))
                }

                // 점수 및 테마 처리 - 항상 표시하되 평가 여부에 따라 다르게
                scoreContainer.visibility = View.VISIBLE

                if (profile.isEvaluated()) {
                    tvScore.text = profile.nameBomScore.toString()
                    applyScoreTheme(profile)
                } else {
                    tvScore.text = "-"
                    // 평가되지 않은 상태의 스타일 적용
                    cardView.setBackgroundColor(itemView.context.getColor(R.color.not_evaluated_bg))
                    scoreContainer.backgroundTintList = itemView.context.getColorStateList(R.color.not_evaluated_accent)
                    ivSprout.setImageResource(R.drawable.ic_seed)
                    ivSprout.imageTintList = itemView.context.getColorStateList(R.color.not_evaluated_icon)
                    tvScore.setTextColor(itemView.context.getColor(R.color.text_secondary))
                }

                // 선택 모드 처리
                checkBox?.isVisible = isSelectionMode
                if (isSelectionMode) {
                    checkBox?.isChecked = selectedIds.contains(profile.id)
                    btnMenu?.isVisible = false
                } else {
                    btnMenu?.isVisible = true
                }

                // 클릭 이벤트
                cardView.setOnClickListener {
                    onItemClick(profile)
                }

                cardView.setOnLongClickListener {
                    onItemLongClick(profile)
                }

                btnMenu?.setOnClickListener {
                    showPopupMenu(it, profile)
                }

                checkBox?.setOnCheckedChangeListener(null)
                checkBox?.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != selectedIds.contains(profile.id)) {
                        toggleSelection(profile.id)
                    }
                }
            }

            private fun formatFullName(profile: Profile): String {
                val surname = profile.surname
                val givenName = profile.givenName

                // 성씨만 있는 경우
                if (surname != null && (givenName == null || givenName.charInfos.isEmpty())) {
                    return "${surname.korean}(${surname.hanja}) ◯◯"
                }

                // 성씨와 이름이 모두 있는 경우
                if (surname != null && givenName != null && givenName.charInfos.isNotEmpty()) {
                    // 이름이 불완전한 경우 처리
                    val givenKorean = givenName.charInfos.joinToString("") {
                        it.korean.ifEmpty { "◯" }
                    }
                    val givenHanja = givenName.charInfos.joinToString("") {
                        it.hanja.ifEmpty { "◯" }
                    }
                    return "${surname.korean}${givenKorean}(${surname.hanja}${givenHanja})"
                }

                // 성씨가 없는 경우
                return "◯ ◯◯"
            }

            private fun applyScoreTheme(profile: Profile) {
                val theme = profile.getScoreThemeColor()
                val context = itemView.context

                // 카드 배경색
                val backgroundColor = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_bg
                    Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_bg
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_bg
                    Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_bg
                    Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_bg
                    Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_bg
                }

                cardView.setBackgroundColor(context.getColor(backgroundColor))

                // 점수 컨테이너 색상
                val accentColor = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
                    Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
                    Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
                    Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
                    Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_accent
                }

                scoreContainer.backgroundTintList = context.getColorStateList(accentColor)

                // 점수에 따른 아이콘 변경
                val iconResource = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.drawable.ic_flower_full
                    Profile.ScoreTheme.WARM_SPRING -> R.drawable.ic_sprout_bloom
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.drawable.ic_sprout
                    Profile.ScoreTheme.RAINY_SPRING -> R.drawable.ic_seed
                    Profile.ScoreTheme.COLD_SPRING -> R.drawable.ic_dormant_seed
                    Profile.ScoreTheme.NOT_EVALUATED -> R.drawable.ic_seed
                }

                ivSprout.setImageResource(iconResource)

                // 아이콘 색상
                val sproutColor = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.color.flower_pink
                    Profile.ScoreTheme.WARM_SPRING -> R.color.leaf_green
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.color.sprout_green
                    Profile.ScoreTheme.RAINY_SPRING -> R.color.seed_brown
                    Profile.ScoreTheme.COLD_SPRING -> R.color.dormant_gray
                    Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_icon
                }

                ivSprout.imageTintList = context.getColorStateList(sproutColor)
            }

            private fun showPopupMenu(view: View, profile: Profile) {
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.profile_item_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(profile)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(profile)
                            true
                        }
                        R.id.action_duplicate -> {
                            duplicateProfile(profile)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }

            private fun duplicateProfile(profile: Profile) {
                val newProfile = profile.copy(
                    id = System.currentTimeMillis().toString(),
                    profileName = "${profile.profileName} (복사본)",
                    createdAt = System.currentTimeMillis()
                )

                if (ProfileManager.addProfile(newProfile)) {
                    currentPage = 1
                    loadProfiles()
                    Snackbar.make(recyclerView, "프로필이 복제되었습니다", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(recyclerView, "프로필 복제에 실패했습니다", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}