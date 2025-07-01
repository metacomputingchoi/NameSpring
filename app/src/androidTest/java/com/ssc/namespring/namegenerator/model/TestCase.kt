// namegenerator/model/TestCase.kt
package com.ssc.namespring.namegenerator.model

import java.time.LocalDateTime

data class TestCase(
    val input: String,
    val description: String? = null,
    val birthDateTime: LocalDateTime? = null,
    val withoutFilter: Boolean = false
)