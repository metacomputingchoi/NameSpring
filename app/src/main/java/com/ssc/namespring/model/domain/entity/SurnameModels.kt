// model/domain/entity/SurnameModels.kt
package com.ssc.namespring.model.domain.entity

import com.google.gson.annotations.SerializedName

data class CharTripleInfoSurname(
    @SerializedName("한글정보")
    val koreanInfo: CharInfo,
    @SerializedName("한자정보")
    val hanjaInfo: CharInfo,
    @SerializedName("통합정보")
    val integratedInfo: IntegratedInfoSurname
)

data class CharInfoSurname(
    @SerializedName("글자")
    val character: String?,
    @SerializedName("뜻")
    val meaning: String?,
    @SerializedName("음")
    val sound: String?,
    @SerializedName("음양")
    val eumyang: Int,
    @SerializedName("오행")
    val ohaeng: String?,
    @SerializedName("획수")
    val strokes: Int,
    @SerializedName("원획수")
    val originalStrokes: Int
)

data class IntegratedInfoSurname(
    @SerializedName("한자")
    val hanja: String,
    @SerializedName("인명용 뜻")
    val nameMeaning: String?,
    @SerializedName("인명용 음")
    val nameSound: String,
    @SerializedName("발음음양")
    val soundEumyang: Int,
    @SerializedName("획수음양")
    val strokeEumyang: Int,
    @SerializedName("발음오행")
    val soundOhaeng: String,
    @SerializedName("자원오행")
    val sourceOhaeng: String,
    @SerializedName("원획수")
    val originalStrokes: Int,
    @SerializedName("옥편획수")
    val dictionaryStrokes: Int,
    @SerializedName("E")
    val englishName: String,
    @SerializedName("CAUTION_RED")
    val cautionRed: String?,
    @SerializedName("CAUTION_BLUE")
    val cautionBlue: String?
)