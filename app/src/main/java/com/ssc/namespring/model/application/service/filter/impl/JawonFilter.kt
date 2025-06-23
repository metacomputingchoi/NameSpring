// model/application/service/filter/impl/JawonFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.util.ElementUtils

class JawonFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val jawon1 = ElementUtils.normalize(name.hanja1Info.jawonOheng ?: "")
        val jawon2 = ElementUtils.normalize(name.hanja2Info.jawonOheng ?: "")

        val jawonCheckResult = checkJawon(jawon1, jawon2, name.zeroElements, name.oneElements)

        return FilterResult(
            passed = jawonCheckResult["passed"] as Boolean,
            details = mapOf("details" to jawonCheckResult)
        )
    }

    override fun getFilterName() = "jawon_check"

    private fun checkJawon(
        jawon1: String,
        jawon2: String,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>(
            "jawon1" to jawon1,
            "jawon2" to jawon2,
            "check_type" to "",
            "passed" to false
        )

        when {
            zeroElements.size == 1 -> {
                result["check_type"] = "single_zero_element"
                if (jawon1 == zeroElements[0] && jawon2 == zeroElements[0]) {
                    result["passed"] = true
                } else {
                    result["reason"] = "expected both ${zeroElements[0]}, got $jawon1 and $jawon2"
                }
            }
            zeroElements.size >= 2 -> {
                result["check_type"] = "multiple_zero_elements"
                var valid = false
                for (i in zeroElements.indices) {
                    for (j in zeroElements.indices) {
                        if (i != j) {
                            val zero1 = zeroElements[i]
                            val zero2 = zeroElements[j]
                            if ((jawon1 == zero1 && jawon2 == zero2) || (jawon1 == zero2 && jawon2 == zero1)) {
                                valid = true
                                result["matched_combination"] = listOf(zero1, zero2)
                                break
                            }
                        }
                    }
                    if (valid) break
                }
                result["passed"] = valid
                if (!valid) {
                    result["reason"] = "no matching combination found for $jawon1 and $jawon2"
                }
            }
            oneElements.size == 1 -> {
                result["check_type"] = "single_one_element"
                if (jawon1 == oneElements[0] || jawon2 == oneElements[0]) {
                    result["passed"] = true
                } else {
                    result["reason"] = "neither is ${oneElements[0]}"
                }
            }
            oneElements.size >= 2 -> {
                result["check_type"] = "multiple_one_elements"
                var valid = false
                for (i in oneElements.indices) {
                    for (j in oneElements.indices) {
                        if (i != j) {
                            val one1 = oneElements[i]
                            val one2 = oneElements[j]
                            if ((jawon1 == one1 && jawon2 == one2) || (jawon1 == one2 && jawon2 == one1)) {
                                valid = true
                                result["matched_combination"] = listOf(one1, one2)
                                break
                            }
                        }
                    }
                    if (valid) break
                }
                result["passed"] = valid
                if (!valid) {
                    result["reason"] = "no matching combination found for $jawon1 and $jawon2"
                }
            }
            else -> {
                result["check_type"] = "no_filter"
                result["passed"] = true
            }
        }

        return result
    }
}