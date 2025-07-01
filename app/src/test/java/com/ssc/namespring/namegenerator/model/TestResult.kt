// namegenerator/model/TestResult.kt
package com.ssc.namespring.namegenerator.model

import com.ssc.namingengine.data.GeneratedName

data class TestResult(
    val testCase: TestCase,
    val results: List<GeneratedName>,
    val elapsedTime: Long,
    val error: String? = null
)