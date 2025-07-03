// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
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
    private lateinit var bottomContainer: LinearLayout
    private lateinit var navigationBarBackground: View
    private var navigationBarHeight = 0

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

    enum class LayoutType {
        LIST, GRID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list_improved)

        setupViews()
        setupRecyclerView()
        setupSearch()
        setupSortChips()
        loadProfiles()
    }

    private fun adjustForSystemUI() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            navigationBarHeight = systemBars.bottom

            // 네비게이션 바 배경 높이 설정
            navigationBarBackground.layoutParams.height = navigationBarHeight

            // 일반 모드에서의 RecyclerView 패딩
            updateRecyclerViewPadding()

            // FAB 위치 조정
            (fabAdd.layoutParams as? CoordinatorLayout.LayoutParams)?.let { params ->
                params.bottomMargin = navigationBarHeight + 24
                fabAdd.layoutParams = params
            }

            // bottomContainer 위치 조정
            (bottomContainer.layoutParams as? CoordinatorLayout.LayoutParams)?.let { params ->
                params.bottomMargin = navigationBarHeight
                bottomContainer.layoutParams = params
            }

            insets
        }
    }

    private fun updateRecyclerViewPadding() {
        val bottomPadding = if (isSelectionMode) {
            // 선택 모드: 전체선택 버튼 + 액션바 + 네비게이션바 높이
            navigationBarHeight + 56 + 56 + 32 // FAB 높이 + 액션바 높이 + 여백
        } else {
            // 일반 모드: FAB + 네비게이션바 높이
            navigationBarHeight + 80
        }

        recyclerView.setPadding(
            recyclerView.paddingLeft,
            recyclerView.paddingTop,
            recyclerView.paddingRight,
            bottomPadding
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isSelectionMode) {
            loadProfiles()
        }
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
            startActivity(Intent(this, ProfileFormActivity::class.java))
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
            startActivity(Intent(this, ProfileFormActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = ProfileAdapter(
            layoutType = currentLayoutType,
            onItemClick = { profile ->
                if (isSelectionMode) {
                    toggleSelection(profile.id)
                } else {
                    ProfileManager.setSelectedProfile(profile)
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
                startActivity(intent)
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

        // 검색 및 정렬
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
                // 취소 시 progressBar 숨기기
                progressBar.isVisible = false
            }
            .setOnCancelListener {
                // 다이얼로그 외부 터치로 취소 시에도 progressBar 숨기기
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

    // enterSelectionMode() 메서드
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

    // exitSelectionMode() 메서드
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

    // updateSelectionUI() 메서드 수정
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
                tvBirthDate.text = profile.getSimpleBirthDate()
                tvBirthTime.text = profile.getBirthTimeString()

                // 사주 정보
                profile.sajuInfo?.let { saju ->
                    tvSaju.text = "사주: ${saju.yearPillar} ${saju.monthPillar} ${saju.dayPillar} ${saju.hourPillar}"
                }

                // 오행 정보
                profile.ohaengInfo?.let { ohaeng ->
                    val lacking = ohaeng.getLackingOhaeng()
                    val excessive = ohaeng.getExcessOhaeng()

                    tvOhaeng.text = when {
                        lacking.isNotEmpty() && excessive.isNotEmpty() ->
                            "부족: ${lacking.joinToString()}, 과다: ${excessive.joinToString()}"
                        lacking.isNotEmpty() ->
                            "부족한 오행: ${lacking.joinToString()}"
                        excessive.isNotEmpty() ->
                            "과다한 오행: ${excessive.joinToString()}"
                        else -> "오행 균형"
                    }
                }

                // 점수 및 테마 색상
                tvScore.text = profile.nameBomScore.toString()
                applyScoreTheme(profile)

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
                    // ProfileManager에 선택된 프로필 설정
                    ProfileManager.switchProfile(profile.id)

                    // MainActivity로 이동
                    val intent = Intent(this@ProfileListActivity, MainActivity::class.java)
                    startActivity(intent)
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

                val surnameText = if (surname != null) {
                    "${surname.korean}(${surname.hanja})"
                } else {
                    "◯"
                }

                if (givenName == null || givenName.charInfos.isEmpty()) {
                    return "$surnameText ◯◯"
                }

                val koreanBuilder = StringBuilder()
                val hanjaBuilder = StringBuilder()

                givenName.charInfos.forEach { charInfo ->
                    koreanBuilder.append(if (charInfo.korean.isNotEmpty()) charInfo.korean else "◯")
                    hanjaBuilder.append(if (charInfo.hanja.isNotEmpty()) charInfo.hanja else "◯")
                }

                while (koreanBuilder.length < 2) {
                    koreanBuilder.append("◯")
                    hanjaBuilder.append("◯")
                }

                return "$surnameText $koreanBuilder($hanjaBuilder)"
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
                }

                cardView.setBackgroundColor(context.getColor(backgroundColor))

                // 점수 컨테이너 색상
                val accentColor = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
                    Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
                    Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
                    Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
                }

                scoreContainer.backgroundTintList = context.getColorStateList(accentColor)

                // 점수에 따른 아이콘 변경
                val iconResource = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.drawable.ic_flower_full
                    Profile.ScoreTheme.WARM_SPRING -> R.drawable.ic_sprout_bloom
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.drawable.ic_sprout
                    Profile.ScoreTheme.RAINY_SPRING -> R.drawable.ic_seed
                    Profile.ScoreTheme.COLD_SPRING -> R.drawable.ic_dormant_seed
                }

                ivSprout.setImageResource(iconResource)

                // 아이콘 색상
                val sproutColor = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> R.color.flower_pink
                    Profile.ScoreTheme.WARM_SPRING -> R.color.leaf_green
                    Profile.ScoreTheme.CLOUDY_SPRING -> R.color.sprout_green
                    Profile.ScoreTheme.RAINY_SPRING -> R.color.seed_brown
                    Profile.ScoreTheme.COLD_SPRING -> R.color.dormant_gray
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