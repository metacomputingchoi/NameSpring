// model/core/NamingSystemConfig.kt
package com.ssc.namespring.model.core

data class NamingSystemConfig(
    val ymdJson: String,
    val nameCharTripleJson: String,
    val surnameCharTripleJson: String,
    val nameKoreanToTripleJson: String,
    val nameHanjaToTripleJson: String,
    val surnameKoreanToTripleJson: String,
    val surnameHanjaToTripleJson: String,
    val surnameHanjaPairJson: String,
    val dictHangulGivenNamesJson: String,
    val surnameChosungToKoreanJson: String
)