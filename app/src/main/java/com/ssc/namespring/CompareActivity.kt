// CompareActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.*
import com.ssc.namespring.ui.compare.adapter.CompareSourceAdapter
import com.ssc.namespring.ui.compare.adapter.CompareTargetAdapter
import com.ssc.namespring.ui.compare.filter.FilterBottomSheet

class CompareActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CompareActivity"
    }

    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var containerComparison: LinearLayout
    private lateinit var containerSource: LinearLayout
    private lateinit var containerTarget: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var chipGroupSort: ChipGroup
    private lateinit var btnAddFilter: ImageButton
    private lateinit var recyclerViewSource: RecyclerView
    private lateinit var recyclerViewTarget: RecyclerView
    private lateinit var emptyViewSource: TextView
    private lateinit var emptyViewTarget: TextView
    private lateinit var fabCompare: ExtendedFloatingActionButton

    // Adapters
    private lateinit var sourceAdapter: CompareSourceAdapter
    private lateinit var targetAdapter: CompareTargetAdapter

    // ViewModel
    private lateinit var viewModel: CompareViewModel
    private lateinit var favoriteRepository: FavoriteNameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        initializeComponents()
        setupViews()
        setupDragAndDrop()
        observeViewModel()
    }

    private fun initializeComponents() {
        viewModel = ViewModelProvider(this).get(CompareViewModel::class.java)
        favoriteRepository = FavoriteNameRepository.getInstance(this)

        // 소스 어댑터 (즐겨찾기 목록)
        sourceAdapter = CompareSourceAdapter(
            onItemClick = { favorite ->
                // 카드 클릭 시 선택/해제 토글
                viewModel.toggleComparison(favorite)
            },
            onFavoriteToggle = { favorite ->
                if (favorite.isDeleted) {
                    // 삭제된 항목 복원
                    viewModel.restoreFavorite(favorite)
                } else {
                    // 즐겨찾기 해제 (선택된 상태에서는 동작하지 않음)
                    viewModel.toggleFavoriteStatus(favorite)
                }
            }
        )

        // 타겟 어댑터 (비교 대상)
        targetAdapter = CompareTargetAdapter(
            onRemoveClick = { favorite ->
                viewModel.removeFromComparison(favorite)
            }
        )
    }

    private fun setupViews() {
        // Find views
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        containerComparison = findViewById(R.id.containerComparison)
        containerSource = findViewById(R.id.containerSource)
        containerTarget = findViewById(R.id.containerTarget)
        searchView = findViewById(R.id.searchView)
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        chipGroupSort = findViewById(R.id.chipGroupSort)
        btnAddFilter = findViewById(R.id.btnAddFilter)
        recyclerViewSource = findViewById(R.id.recyclerViewSource)
        recyclerViewTarget = findViewById(R.id.recyclerViewTarget)
        emptyViewSource = findViewById(R.id.emptyViewSource)
        emptyViewTarget = findViewById(R.id.emptyViewTarget)
        fabCompare = findViewById(R.id.fabCompare)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "이름 비교"
        }

        // Setup tabs
        setupTabs()

        // Setup RecyclerViews
        recyclerViewSource.apply {
            layoutManager = LinearLayoutManager(this@CompareActivity)
            adapter = sourceAdapter
        }

        recyclerViewTarget.apply {
            layoutManager = LinearLayoutManager(this@CompareActivity)
            adapter = targetAdapter
        }

        // Setup search
        setupSearch()

        // Setup sort chips
        setupSortChips()

        // Setup filter button
        btnAddFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        // Setup compare button
        fabCompare.setOnClickListener {
            performComparison()
        }

        // 비교 모드 초기 설정
        updateComparisonMode(false)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("즐겨찾기"))
        tabLayout.addTab(tabLayout.newTab().setText("삭제됨"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val showDeleted = tab.position == 1
                viewModel.setShowDeleted(showDeleted)
                // 어댑터에 삭제됨 탭 상태 전달
                sourceAdapter.setShowDeleted(showDeleted)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupSearch() {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupSortChips() {
        val sortOptions = listOf(
            CompareViewModel.SortType.NAME to "이름순",
            CompareViewModel.SortType.SCORE to "점수순",
            CompareViewModel.SortType.BIRTH_DATE to "생년월일순",
            CompareViewModel.SortType.ADDED_DATE to "추가일순"
        )

        sortOptions.forEach { (sortType, label) ->
            val chip = createSortChip(sortType, label)
            chipGroupSort.addView(chip)
        }
    }

    private fun createSortChip(sortType: CompareViewModel.SortType, label: String): View {
        val chipLayout = LayoutInflater.from(this)
            .inflate(R.layout.item_sort_chip, chipGroupSort, false)

        val chip = chipLayout.findViewById<Chip>(R.id.chip)
        val btnSort = chipLayout.findViewById<ImageButton>(R.id.btnSort)

        chip.text = label
        chip.isCheckable = true
        chip.tag = sortType  // sortType을 태그로 저장

        // 기본 정렬 없음 - 모든 칩이 선택되지 않은 상태로 시작
        chip.isChecked = false
        btnSort.visibility = View.GONE

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleSort(sortType)
                btnSort.visibility = View.VISIBLE
                updateSortButton(btnSort, sortType)
            } else {
                viewModel.removeSort(sortType)
                btnSort.visibility = View.GONE
            }
        }

        btnSort.setOnClickListener {
            viewModel.toggleSort(sortType)
            updateSortButton(btnSort, sortType)
        }

        return chipLayout
    }
    private fun updateSortButton(button: ImageButton, sortType: CompareViewModel.SortType) {
        val sorts = viewModel.activeSorts.value ?: return
        val sortInfo = sorts.find { it.type == sortType } ?: return

        button.setImageResource(
            if (sortInfo.ascending) R.drawable.ic_arrow_upward
            else R.drawable.ic_arrow_downward
        )
    }

    private fun setupDragAndDrop() {
        // 드래그앤드롭으로 비교 대상 추가
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favorite = sourceAdapter.currentList[position]
                viewModel.addToComparison(favorite)

                // 스와이프 후 원래 위치로 복원
                sourceAdapter.notifyItemChanged(position)
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // 삭제된 항목은 스와이프 불가
                val position = viewHolder.adapterPosition
                val favorite = sourceAdapter.currentList.getOrNull(position)
                return if (favorite?.isDeleted == true) 0 else super.getSwipeDirs(recyclerView, viewHolder)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerViewSource)
    }

    private fun observeViewModel() {
        // 필터링된 즐겨찾기 목록
        viewModel.filteredFavorites.observe(this) { favorites ->
            sourceAdapter.submitList(favorites)
            updateEmptyView(emptyViewSource, favorites.isEmpty(), "source")
        }

        // 비교 대상 목록
        viewModel.comparisonList.observe(this) { comparisonList ->
            targetAdapter.submitList(comparisonList)
            updateEmptyView(emptyViewTarget, comparisonList.isEmpty(), "target")

            // 선택된 항목들의 키 세트 업데이트
            val selectedKeys = comparisonList.map { it.getKey() }.toSet()
            sourceAdapter.updateSelectedItems(selectedKeys)

            // FAB 업데이트
            updateCompareButton(comparisonList.size)

            // 비교 모드 업데이트
            updateComparisonMode(comparisonList.isNotEmpty())

            // 선택된 항목 정보 표시
            updateSelectionInfo(comparisonList)
        }

        // 활성 필터
        viewModel.activeFilters.observe(this) { filters ->
            updateFilterChips(filters)
        }

        // 활성 정렬
        viewModel.activeSorts.observe(this) { sorts ->
            updateSortChips(sorts)
        }
    }

    private fun updateSortChips(sorts: List<CompareViewModel.SortInfo>) {
        // 정렬 칩들의 상태 업데이트
        for (i in 0 until chipGroupSort.childCount) {
            val chipLayout = chipGroupSort.getChildAt(i)
            val chip = chipLayout.findViewById<Chip>(R.id.chip)
            val btnSort = chipLayout.findViewById<ImageButton>(R.id.btnSort)

            // 태그에 저장된 sortType 가져오기
            val sortType = chip.tag as? CompareViewModel.SortType ?: continue
            val sortInfo = sorts.find { it.type == sortType }

            if (sortInfo != null) {
                chip.isChecked = true
                btnSort.visibility = View.VISIBLE
                btnSort.setImageResource(
                    if (sortInfo.ascending) R.drawable.ic_arrow_upward
                    else R.drawable.ic_arrow_downward
                )
            } else {
                chip.isChecked = false
                btnSort.visibility = View.GONE
            }
        }
    }

    private fun updateSelectionInfo(selectedItems: List<FavoriteName>) {
        val selectionInfo = findViewById<TextView>(R.id.tvSelectionInfo)
        if (selectedItems.isNotEmpty()) {
            selectionInfo?.apply {
                visibility = View.VISIBLE
                text = "선택된 이름: ${selectedItems.joinToString(", ") { it.fullNameKorean }}"
            }
        } else {
            selectionInfo?.visibility = View.GONE
        }
    }

    private fun updateEmptyView(emptyView: TextView, isEmpty: Boolean, type: String) {
        if (isEmpty) {
            emptyView.visibility = View.VISIBLE
            emptyView.text = when (type) {
                "source" -> when {
                    viewModel.showDeleted.value == true -> "삭제된 즐겨찾기가 없습니다"
                    viewModel.hasActiveFilters() -> "필터 조건에 맞는 이름이 없습니다"
                    else -> "즐겨찾기된 이름이 없습니다"
                }
                "target" -> "비교할 이름을 선택하세요\n(오른쪽으로 스와이프)"
                else -> ""
            }
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private fun updateCompareButton(count: Int) {
        when {
            count == 0 -> {
                fabCompare.text = "비교할 이름 선택"
                fabCompare.isEnabled = false
                fabCompare.hide()
            }
            count == 1 -> {
                fabCompare.text = "1개 더 선택 필요"
                fabCompare.isEnabled = false
                fabCompare.show()
            }
            else -> {
                fabCompare.text = "${count}개 이름 비교하기"
                fabCompare.isEnabled = true
                fabCompare.show()
            }
        }
    }

    private fun updateComparisonMode(hasComparison: Boolean) {
        if (hasComparison) {
            // 분할 뷰 표시
            containerSource.layoutParams = (containerSource.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
            }
            containerTarget.visibility = View.VISIBLE
        } else {
            // 전체 뷰로 표시
            containerSource.layoutParams = (containerSource.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
            containerTarget.visibility = View.GONE
        }
    }

    private fun updateFilterChips(filters: List<CompareViewModel.FilterInfo>) {
        chipGroupFilters.removeAllViews()

        filters.forEach { filterInfo ->
            val chip = Chip(this).apply {
                text = filterInfo.displayName
                isCloseIconVisible = true
                setChipBackgroundColorResource(R.color.chip_background)
                setOnCloseIconClickListener {
                    viewModel.removeFilter(filterInfo.id)
                }
            }
            chipGroupFilters.addView(chip)
        }
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet(this) { filters ->
            filters.forEach { (type, value) ->
                viewModel.addFilter(type, value)
            }
        }.show()
    }

    private fun performComparison() {
        val comparisonList = viewModel.comparisonList.value ?: emptyList()
        if (comparisonList.size >= 2) {
            CompareResultDialog(this, comparisonList).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_compare, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_comparison -> {
                viewModel.clearComparison()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}