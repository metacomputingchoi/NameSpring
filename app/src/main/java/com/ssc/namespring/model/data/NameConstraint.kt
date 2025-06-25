// model/data/NameConstraint.kt
package com.ssc.namespring.model.data

import com.ssc.namespring.model.Constants.ConstraintTypes

data class NameConstraint(
    val hangulType: String,
    val hangulValue: String?,
    val hanjaType: String,
    val hanjaValue: String?
) {
    fun isAllEmpty() = hangulType == ConstraintTypes.EMPTY && hanjaType == ConstraintTypes.EMPTY
}
