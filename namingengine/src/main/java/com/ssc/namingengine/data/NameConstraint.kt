// data/NameConstraint.kt
package com.ssc.namingengine.data

import com.ssc.namingengine.common.parsing.ParsingConstants.ConstraintTypes

data class NameConstraint(
    val hangulType: String,
    val hangulValue: String?,
    val hanjaType: String,
    val hanjaValue: String?
) {
    fun isAllEmpty() = hangulType == ConstraintTypes.EMPTY && hanjaType == ConstraintTypes.EMPTY
}
