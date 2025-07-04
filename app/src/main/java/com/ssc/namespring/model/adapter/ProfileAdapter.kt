package com.ssc.namespring.model.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.R
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.repository.ProfileManager
import java.util.Calendar

class ProfileAdapter(
    private val onItemClick: (Profile) -> Unit,
    private val onItemLongClick: (Profile) -> Boolean,
    private val onEditClick: (Profile) -> Unit,
    private val onDeleteClick: (Profile) -> Unit,
    private val onDuplicateClick: (Profile) -> Unit
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

    fun getSelectableItemCount() = profiles.size
    fun getSelectableIds() = profiles.map { it.id }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_improved_list, parent, false)
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
        private val tvSproutIcon: TextView = itemView.findViewById(R.id.tvSproutIcon)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val btnMenu: ImageButton? = itemView.findViewById(R.id.btnMenu)
        private val scoreContainer: View = itemView.findViewById(R.id.scoreContainer)

        // 오행 분포 표시 (리스트 뷰에만 있을 수 있음)
        private val tvWoodDist: TextView? = itemView.findViewById(R.id.tvWoodDist)
        private val tvFireDist: TextView? = itemView.findViewById(R.id.tvFireDist)
        private val tvEarthDist: TextView? = itemView.findViewById(R.id.tvEarthDist)
        private val tvMetalDist: TextView? = itemView.findViewById(R.id.tvMetalDist)
        private val tvWaterDist: TextView? = itemView.findViewById(R.id.tvWaterDist)
        private val ohaengDistribution: LinearLayout? = itemView.findViewById(R.id.ohaengDistribution)

        fun bind(profile: Profile) {
            // 기본 정보
            tvProfileName.text = profile.profileName
            tvFullName.text = formatFullName(profile)

            // 날짜 포맷
            val cal = profile.birthDate
            tvBirthDate.text = String.format("%d.%02d.%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH))

            tvBirthTime.text = String.format("%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE))

            // 사주 정보
            profile.sajuInfo?.let { saju ->
                tvSaju.text = saju.fourPillars.joinToString(" ")
                tvSaju.visibility = View.VISIBLE
            } ?: run {
                tvSaju.visibility = View.GONE
            }

            // 오행 정보
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

            // 점수 및 테마 처리
            scoreContainer.visibility = View.VISIBLE

            if (profile.isEvaluated()) {
                tvScore.text = profile.nameBomScore.toString()
                applyScoreTheme(profile)
            } else {
                tvScore.text = "-"
                tvSproutIcon.text = itemView.context.getString(R.string.icon_dormant_seed)
                cardView.setBackgroundColor(itemView.context.getColor(R.color.not_evaluated_bg))
                scoreContainer.backgroundTintList = itemView.context.getColorStateList(R.color.not_evaluated_accent)
                tvScore.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }

            // 선택 모드 처리
            checkBox?.let { cb ->
                cb.visibility = if (isSelectionMode) View.VISIBLE else View.GONE

                if (isSelectionMode) {
                    // 리스너를 먼저 제거하여 순환 호출 방지
                    cb.setOnCheckedChangeListener(null)
                    cb.isChecked = selectedIds.contains(profile.id)

                    // 체크박스 클릭 리스너 설정
                    cb.setOnCheckedChangeListener { _, isChecked ->
                        // 실제 상태와 다를 때만 처리
                        if (isChecked != selectedIds.contains(profile.id)) {
                            onItemClick(profile)
                        }
                    }

                    btnMenu?.visibility = View.GONE
                } else {
                    btnMenu?.visibility = View.VISIBLE
                }
            }

            // 카드뷰 클릭 이벤트 - 체크박스가 아닌 영역 클릭 시에만 동작
            cardView.setOnClickListener { view ->
                // 체크박스를 직접 클릭한 경우가 아닐 때만 처리
                if (!isSelectionMode || view !is CheckBox) {
                    onItemClick(profile)
                }
            }

            cardView.setOnLongClickListener {
                onItemLongClick(profile)
            }

            btnMenu?.setOnClickListener {
                showPopupMenu(it, profile)
            }
        }

        private fun formatFullName(profile: Profile): String {
            val surname = profile.surname
            val givenName = profile.givenName

            if (surname != null && (givenName == null || givenName.charInfos.isEmpty())) {
                return "${surname.korean}(${surname.hanja}) ◯◯"
            }

            if (surname != null && givenName != null && givenName.charInfos.isNotEmpty()) {
                val givenKorean = givenName.charInfos.joinToString("") {
                    it.korean.ifEmpty { "◯" }
                }
                val givenHanja = givenName.charInfos.joinToString("") {
                    it.hanja.ifEmpty { "◯" }
                }
                return "${surname.korean}${givenKorean}(${surname.hanja}${givenHanja})"
            }

            return "-"
        }

        private fun applyScoreTheme(profile: Profile) {
            val theme = profile.getScoreThemeColor()
            val context = itemView.context

            val backgroundColor = when (theme) {
                Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_bg
                Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_bg
                Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_bg
                Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_bg
                Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_bg
                Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_bg
            }

            cardView.setBackgroundColor(context.getColor(backgroundColor))

            val accentColor = when (theme) {
                Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
                Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
                Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
                Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
                Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
                Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_accent
            }

            scoreContainer.backgroundTintList = context.getColorStateList(accentColor)

            val emojiResId = when (theme) {
                Profile.ScoreTheme.SUNNY_SPRING -> R.string.icon_flower_full
                Profile.ScoreTheme.WARM_SPRING -> R.string.icon_sprout_bloom
                Profile.ScoreTheme.CLOUDY_SPRING -> R.string.icon_sprout
                Profile.ScoreTheme.RAINY_SPRING -> R.string.icon_seed
                Profile.ScoreTheme.COLD_SPRING -> R.string.icon_dormant_seed
                Profile.ScoreTheme.NOT_EVALUATED -> R.string.icon_dormant_seed
            }
            tvSproutIcon.text = context.getString(emojiResId)
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
                Snackbar.make(
                    itemView,
                    "프로필이 복제되었습니다",
                    Snackbar.LENGTH_SHORT
                ).show()
                onDuplicateClick(profile)  // 복제 성공 시 콜백 호출
            } else {
                Snackbar.make(
                    itemView,
                    "프로필 복제에 실패했습니다",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}