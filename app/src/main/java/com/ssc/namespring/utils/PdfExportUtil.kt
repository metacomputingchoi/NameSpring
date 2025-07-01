// utils/PdfExportUtil.kt
package com.ssc.namespring.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.data.ComparisonReport
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * PDF 보고서 생성 유틸리티
 * 평가 결과와 비교 결과를 PDF로 내보내기
 */
object PdfExportUtil {

    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 50
    private const val LINE_HEIGHT = 20

    fun exportEvaluationReport(
        context: Context,
        report: EvaluationReport
    ): Result<File> {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawEvaluationReport(canvas, report)

            document.finishPage(page)

            val file = saveDocument(context, document, "evaluation_${report.id}.pdf")
            document.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportComparisonReport(
        context: Context,
        report: ComparisonReport
    ): Result<File> {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawComparisonReport(canvas, report)

            document.finishPage(page)

            val file = saveDocument(context, document, "comparison_${report.id}.pdf")
            document.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawEvaluationReport(canvas: Canvas, report: EvaluationReport) {
        val titlePaint = Paint().apply {
            textSize = 24f
            color = Color.BLACK
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 18f
            color = Color.DKGRAY
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }

        var y = MARGIN.toFloat()

        // 제목
        canvas.drawText("이름 평가 보고서", MARGIN.toFloat(), y, titlePaint)
        y += LINE_HEIGHT * 2

        // 이름 정보
        canvas.drawText("이름: ${report.getDisplayName()} (${report.getDisplayHanja()})", MARGIN.toFloat(), y, subtitlePaint)
        y += LINE_HEIGHT
        canvas.drawText("평가 대상: ${report.profile.profileName}", MARGIN.toFloat(), y, textPaint)
        y += LINE_HEIGHT
        canvas.drawText("평가 일시: ${report.createdAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))}", MARGIN.toFloat(), y, textPaint)
        y += LINE_HEIGHT * 2

        // 종합 점수
        canvas.drawText("종합 점수: ${report.overallScore}점", MARGIN.toFloat(), y, titlePaint)
        y += LINE_HEIGHT * 2

        // 항목별 점수
        canvas.drawText("【항목별 평가】", MARGIN.toFloat(), y, subtitlePaint)
        y += LINE_HEIGHT

        val scoreItems = listOf(
            "사주 보완도" to report.sajuCompensation,
            "음양 균형도" to report.yinYangBalance,
            "오행 조화도" to report.fiveElementsHarmony,
            "획수 길흉" to report.strokeAuspiciousness,
            "발음 자연스러움" to report.pronunciationNaturalness
        )

        scoreItems.forEach { (name, detail) ->
            canvas.drawText("$name: ${detail.score}점", MARGIN.toFloat() + 20, y, textPaint)
            y += LINE_HEIGHT

            // 상세 설명 (줄바꿈 처리)
            val lines = wrapText(detail.description, 60)
            lines.forEach { line ->
                canvas.drawText(line, MARGIN.toFloat() + 40, y, textPaint)
                y += LINE_HEIGHT
            }
            y += LINE_HEIGHT / 2
        }

        // 추천사항
        if (report.recommendations.isNotEmpty()) {
            y += LINE_HEIGHT
            canvas.drawText("【추천사항】", MARGIN.toFloat(), y, subtitlePaint)
            y += LINE_HEIGHT

            report.recommendations.forEach { rec ->
                canvas.drawText("• $rec", MARGIN.toFloat() + 20, y, textPaint)
                y += LINE_HEIGHT
            }
        }
    }

    private fun drawComparisonReport(canvas: Canvas, report: ComparisonReport) {
        val titlePaint = Paint().apply {
            textSize = 24f
            color = Color.BLACK
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 18f
            color = Color.DKGRAY
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }

        var y = MARGIN.toFloat()

        // 제목
        canvas.drawText("이름 비교 보고서", MARGIN.toFloat(), y, titlePaint)
        y += LINE_HEIGHT * 2

        // 비교 대상
        canvas.drawText("비교 기준: ${report.profile.profileName}", MARGIN.toFloat(), y, textPaint)
        y += LINE_HEIGHT
        canvas.drawText("비교 일시: ${report.createdAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))}", MARGIN.toFloat(), y, textPaint)
        y += LINE_HEIGHT * 2

        // 최종 순위
        canvas.drawText("【최종 순위】", MARGIN.toFloat(), y, subtitlePaint)
        y += LINE_HEIGHT

        report.rankings.forEach { ranking ->
            val medal = when (ranking.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "${ranking.rank}위"
            }

            canvas.drawText("$medal ${ranking.getDisplayName()} - ${ranking.totalScore}점", MARGIN.toFloat() + 20, y, textPaint)
            y += LINE_HEIGHT

            if (ranking.strengths.isNotEmpty()) {
                canvas.drawText("강점: ${ranking.strengths.joinToString(", ")}", MARGIN.toFloat() + 40, y, textPaint)
                y += LINE_HEIGHT
            }

            y += LINE_HEIGHT / 2
        }

        // 최종 추천
        y += LINE_HEIGHT
        canvas.drawText("최종 추천: ${report.winnerName.surnameHangul}${report.winnerName.combinedPronounciation}", MARGIN.toFloat(), y, titlePaint)
    }

    private fun saveDocument(context: Context, document: PdfDocument, filename: String): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "namespring")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, filename)
        val outputStream = FileOutputStream(file)
        document.writeTo(outputStream)
        outputStream.close()

        return file
    }

    private fun wrapText(text: String, maxLength: Int): List<String> {
        if (text.length <= maxLength) return listOf(text)

        val lines = mutableListOf<String>()
        var currentLine = ""

        text.split(" ").forEach { word ->
            if (currentLine.length + word.length + 1 > maxLength) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}