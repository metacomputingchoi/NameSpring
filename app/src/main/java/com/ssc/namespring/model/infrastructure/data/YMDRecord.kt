// model/infrastructure/data/YMDRecord.kt
package com.ssc.namespring.model.infrastructure.data

data class YMDRecord(
    val year: Int, 
    val month: Int, 
    val day: Int,
    val yeonju: String, 
    val wolju: String, 
    val ilju: String
)