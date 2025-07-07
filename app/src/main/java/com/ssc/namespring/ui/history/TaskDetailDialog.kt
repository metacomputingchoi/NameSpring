// ui/history/TaskDetailDialog.kt
package com.ssc.namespring.ui.history

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskResult
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailDialog(
    context: Context,
    private val task: Task,
    private val taskRepository: TaskRepository
) : Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 다이얼로그를 화면 크기의 90%로 설정
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 스크롤 가능한 레이아웃 생성
        val scrollView = NestedScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        scrollView.addView(containerLayout)
        setContentView(scrollView)

        setupViews(containerLayout)
    }

    private fun setupViews(container: LinearLayout) {
        // 제목
        container.addView(createTitleView())
        container.addView(createDivider())

        // 기본 정보
        container.addView(createSectionTitle("기본 정보"))
        container.addView(createInfoRow("작업 ID", task.id))
        container.addView(createInfoRow("작업 유형", getTaskTypeName(task.type)))
        container.addView(createInfoRow("상태", task.status.name))
        container.addView(createInfoRow("생성 시간", formatDateTime(task.createdAt)))

        task.startedAt?.let {
            container.addView(createInfoRow("시작 시간", formatDateTime(it)))
        }

        task.completedAt?.let {
            container.addView(createInfoRow("완료 시간", formatDateTime(it)))
        }

        task.getDuration()?.let {
            container.addView(createInfoRow("소요 시간", formatDuration(it)))
        }

        container.addView(createDivider())

        // 입력 데이터
        container.addView(createSectionTitle("입력 데이터"))
        container.addView(createCodeView(gson.toJson(task.inputData)))

        // 결과 데이터 또는 에러
        when (task.status) {
            TaskStatus.COMPLETED -> {
                val result = taskRepository.getTaskResult(task.id)
                result?.let {
                    container.addView(createDivider())
                    container.addView(createSectionTitle("결과 데이터"))

                    // 요약 데이터 표시
                    if (it.data != null) {
                        container.addView(createCodeView(gson.toJson(it.data)))
                    }

                    // Raw data 처리
                    handleRawData(container, it)
                }
            }
            TaskStatus.FAILED -> {
                task.errorMessage?.let {
                    container.addView(createDivider())
                    container.addView(createSectionTitle("에러 메시지"))
                    container.addView(createErrorView(it))
                }
            }
            else -> {}
        }

        container.addView(createDivider())

        // 버튼들
        container.addView(createButtonsLayout())
    }

    private fun handleRawData(container: LinearLayout, result: TaskResult) {
        // Raw data가 있는 경우
        if (result.rawData != null) {
            processRawData(container, result.rawData)
        }
        // Raw data가 파일로 저장된 경우
        else if (result.data?.containsKey("rawDataFile") == true) {
            val filePath = result.data["rawDataFile"] as? String
            if (filePath != null) {
                scope.launch {
                    val rawData = taskRepository.loadRawDataFromFile(filePath)
                    rawData?.let {
                        processRawData(container, it)
                    }
                }
            }
        }
    }

    private fun processRawData(container: LinearLayout, rawData: String) {
        container.addView(createDivider())
        container.addView(createSectionTitle("전체 결과 데이터"))

        // 작명 결과인 경우 특별 처리
        if (task.type == TaskType.NAMING) {
            try {
                val type = object : TypeToken<List<GeneratedName>>() {}.type
                val generatedNames: List<GeneratedName> = gson.fromJson(rawData, type)

                container.addView(createInfoRow("생성된 이름 수", "${generatedNames.size}개"))

                // 상위 10개만 보여주기
                val namesToShow = generatedNames.take(10)
                container.addView(createSectionTitle("상위 ${namesToShow.size}개 이름"))

                namesToShow.forEachIndexed { index, name ->
                    container.addView(createNameView(index + 1, name))
                }

                if (generatedNames.size > 10) {
                    container.addView(createInfoRow("", "... 외 ${generatedNames.size - 10}개"))
                }
            } catch (e: Exception) {
                // 파싱 실패시 원본 표시
                container.addView(createCodeView(rawData.take(1000) + if (rawData.length > 1000) "\n... (전체 ${rawData.length}자)" else ""))
            }
        } else {
            // 다른 타입의 경우 JSON 표시
            container.addView(createCodeView(rawData.take(1000) + if (rawData.length > 1000) "\n... (전체 ${rawData.length}자)" else ""))
        }

        // Raw JSON 보기/복사 버튼
        container.addView(createRawDataButtons(rawData))
    }

    private fun createNameView(index: Int, name: GeneratedName): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)

            // 이름 정보
            addView(TextView(context).apply {
                text = "$index. ${name.combinedPronounciation} (${name.combinedHanja})"
                textSize = 16f
                setTextColor(context.getColor(android.R.color.black))
            })

            // 점수 정보
            name.analysisInfo?.let { info ->
                addView(TextView(context).apply {
                    text = "총점: ${info.totalScore}점"
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }

            // 의미 정보
            val meaning = name.hanjaDetails
                .drop(1) // 성씨 제외
                .mapNotNull { it.inmyongMeaning.takeIf { meaning -> meaning.isNotBlank() } }
                .joinToString(", ")

            if (meaning.isNotEmpty()) {
                addView(TextView(context).apply {
                    text = "의미: $meaning"
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }
        }
    }

    private fun createRawDataButtons(rawData: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)

            // JSON 전체 보기 버튼
            addView(MaterialButton(context).apply {
                text = "JSON 전체 보기"
                setOnClickListener {
                    showFullJsonDialog(rawData)
                }
            })

            // 클립보드 복사 버튼
            addView(MaterialButton(context).apply {
                text = "JSON 복사"
                setOnClickListener {
                    copyToClipboard(rawData)
                }
            })
        }
    }

    private fun showFullJsonDialog(jsonData: String) {
        val dialog = Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert)

        val scrollView = ScrollView(context)
        val textView = TextView(context).apply {
            text = jsonData
            setPadding(32, 32, 32, 32)
            textSize = 12f
            setTextIsSelectable(true)
        }

        scrollView.addView(textView)
        dialog.setContentView(scrollView)
        dialog.setTitle("전체 JSON 데이터")

        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Task Result JSON", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "JSON이 클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun createTitleView(): TextView {
        return TextView(context).apply {
            text = "작업 상세 정보"
            textSize = 20f
            setTextColor(context.getColor(android.R.color.black))
            setPadding(0, 0, 0, 16)
        }
    }

    private fun createSectionTitle(title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 16f
            setTextColor(context.getColor(R.color.primary_blue))
            setPadding(0, 24, 0, 8)
        }
    }

    private fun createInfoRow(label: String, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4, 0, 4)

            if (label.isNotEmpty()) {
                addView(TextView(context).apply {
                    text = "$label: "
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }

            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(context.getColor(android.R.color.black))
            })
        }
    }

    private fun createCodeView(code: String): HorizontalScrollView {
        return HorizontalScrollView(context).apply {
            addView(TextView(context).apply {
                text = code
                textSize = 12f
                setTextColor(context.getColor(android.R.color.black))
                setBackgroundColor(context.getColor(android.R.color.darker_gray))
                setPadding(16, 16, 16, 16)
                setTextIsSelectable(true)
            })
        }
    }

    private fun createErrorView(error: String): TextView {
        return TextView(context).apply {
            text = error
            textSize = 14f
            setTextColor(context.getColor(R.color.error_red))
            setPadding(0, 8, 0, 8)
        }
    }

    private fun createDivider(): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(context.getColor(android.R.color.darker_gray))
        }
    }

    private fun createButtonsLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END

            addView(MaterialButton(context).apply {
                text = "닫기"
                setOnClickListener { dismiss() }
            })
        }
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서 생성"
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        return when {
            seconds < 60 -> "${seconds}초"
            seconds < 3600 -> "${seconds / 60}분 ${seconds % 60}초"
            else -> "${seconds / 3600}시간 ${(seconds % 3600) / 60}분"
        }
    }
}