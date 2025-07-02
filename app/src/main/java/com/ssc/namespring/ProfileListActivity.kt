// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssc.namespring.model.Profile
import com.ssc.namespring.model.ProfileManager
import java.text.SimpleDateFormat
import java.util.*

class ProfileListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list)

        setupViews()
        loadProfiles()
    }

    override fun onResume() {
        super.onResume()
        loadProfiles()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProfileAdapter(
            onItemClick = { profile ->
                // 프로필 선택 시 MainActivity로 이동
                ProfileManager.setSelectedProfile(profile)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            },
            onEditClick = { profile ->
                // 프로필 편집
                val intent = Intent(this, ProfileFormActivity::class.java)
                intent.putExtra("profileId", profile.id)
                startActivity(intent)
            },
            onDeleteClick = { profile ->
                // 프로필 삭제 확인 다이얼로그
                showDeleteConfirmDialog(profile)
            }
        )

        recyclerView.adapter = adapter

        // FAB 클릭 - 새 프로필 추가
        fabAdd.setOnClickListener {
            val intent = Intent(this, ProfileFormActivity::class.java)
            startActivity(intent)
        }

        // 빈 화면의 버튼 클릭
        findViewById<Button>(R.id.btnCreateProfile).setOnClickListener {
            val intent = Intent(this, ProfileFormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfiles() {
        val profiles = ProfileManager.getAllProfiles()
        adapter.submitList(profiles)

        if (profiles.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            fabAdd.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            fabAdd.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmDialog(profile: Profile) {
        AlertDialog.Builder(this)
            .setTitle("프로필 삭제")
            .setMessage("'${profile.profileName}' 프로필을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                ProfileManager.deleteProfile(profile.id)
                loadProfiles()
                Toast.makeText(this, "프로필이 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 프로필 어댑터
    inner class ProfileAdapter(
        private val onItemClick: (Profile) -> Unit,
        private val onEditClick: (Profile) -> Unit,
        private val onDeleteClick: (Profile) -> Unit
    ) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

        private var profiles = listOf<Profile>()

        fun submitList(list: List<Profile>) {
            profiles = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_with_menu, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(profiles[position])
        }

        override fun getItemCount() = profiles.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardView: View = itemView.findViewById(R.id.cardView)
            private val tvProfileName: TextView = itemView.findViewById(R.id.tvProfileName)
            private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
            private val tvBirthDate: TextView = itemView.findViewById(R.id.tvBirthDate)
            private val tvLackingOhaeng: TextView = itemView.findViewById(R.id.tvLackingOhaeng)
            private val ivSprout: ImageView = itemView.findViewById(R.id.ivSprout)
            private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
            private val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenu)

            fun bind(profile: Profile) {
                tvProfileName.text = profile.profileName

                // 실제 이름 표시
                val fullName = formatFullName(profile)
                tvFullName.text = fullName

                val dateFormat = SimpleDateFormat("yyyy년 M월 d일생", Locale.KOREAN)
                tvBirthDate.text = dateFormat.format(profile.birthDate.time)

                // 점수 계산 (임시)
                val score = (70..99).random()
                tvScore.text = score.toString()

                // 부족한 오행 계산 (임시)
                val ohaengs = listOf("목(木)", "화(火)", "토(土)", "금(金)", "수(水)")
                tvLackingOhaeng.text = "부족한 오행: ${ohaengs.random()}"

                // 카드 클릭 - 프로필 선택
                cardView.setOnClickListener {
                    onItemClick(profile)
                }

                // 메뉴 버튼 클릭
                btnMenu.setOnClickListener {
                    showPopupMenu(it, profile)
                }
            }

            private fun formatFullName(profile: Profile): String {
                val surname = profile.surname
                val givenName = profile.givenName

                // 성씨
                val surnameText = "${surname?.korean}(${surname?.hanja})"

                // 이름이 없는 경우
                if (givenName == null) {
                    return "$surnameText ◇◇"  // 기본 2글자 표시
                }

                // charInfos를 기준으로 처리 (실제 저장된 글자 정보)
                if (givenName.charInfos.isEmpty()) {
                    return "$surnameText ◇◇"
                }

                // 이름 부분 처리
                val koreanBuilder = StringBuilder()
                val hanjaBuilder = StringBuilder()

                // charInfos의 각 항목을 처리
                givenName.charInfos.forEach { charInfo ->
                    // 한글
                    if (charInfo.korean.isNotEmpty()) {
                        koreanBuilder.append(charInfo.korean)
                    } else {
                        koreanBuilder.append("◇")
                    }

                    // 한자
                    if (charInfo.hanja.isNotEmpty()) {
                        hanjaBuilder.append(charInfo.hanja)
                    } else {
                        hanjaBuilder.append("◇")
                    }
                }

                // 최소 2글자 표시를 위해 부족한 부분 채우기
                while (koreanBuilder.length < 2) {
                    koreanBuilder.append("◇")
                    hanjaBuilder.append("◇")
                }

                return "$surnameText $koreanBuilder($hanjaBuilder)"
            }

            private fun showPopupMenu(view: View, profile: Profile) {
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.profile_menu)

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
                        else -> false
                    }
                }

                popup.show()
            }
        }
    }
}