// model/data/source/ReportData.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class ReportTemplates(
    @SerializedName("report_headers")
    val reportHeaders: Map<String, Any>,

    @SerializedName("section_titles")
    val sectionTitles: Map<String, String>,

    @SerializedName("subsection_labels")
    val subsectionLabels: Map<String, String>,

    @SerializedName("footer_messages")
    val footerMessages: Map<String, String>,

    @SerializedName("score_display")
    val scoreDisplay: Map<String, String>
)

data class BasicReportSections(
    val sections: List<String>,

    @SerializedName("score_categories")
    val scoreCategories: List<ScoreCategory>
)

data class ScoreCategory(
    val name: String,

    @SerializedName("max_score")
    val maxScore: Int,

    val field: String
)

data class FormatSettings(
    @SerializedName("progress_bar")
    val progressBar: ProgressBarSettings,

    @SerializedName("dot_display")
    val dotDisplay: DotDisplaySettings,

    val separators: Map<String, String>,
    val prefixes: Map<String, String>,

    @SerializedName("section_numbers")
    val sectionNumbers: Map<String, String>
)

data class ProgressBarSettings(
    @SerializedName("filled_bar")
    val filledBar: String,

    @SerializedName("empty_bar")
    val emptyBar: String,

    @SerializedName("bar_length")
    val barLength: Int,

    @SerializedName("bar_unit")
    val barUnit: Int
)

data class DotDisplaySettings(
    @SerializedName("filled_dot")
    val filledDot: String,

    @SerializedName("empty_dot")
    val emptyDot: String,

    @SerializedName("max_dots")
    val maxDots: Int
)
