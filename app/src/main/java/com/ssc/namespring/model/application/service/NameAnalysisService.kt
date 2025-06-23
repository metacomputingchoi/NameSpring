// model/application/service/NameAnalysisService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.domain.name.entity.NameCombination
import com.ssc.namespring.model.domain.name.entity.ElementCheck

class NameAnalysisService {

    fun mapToNameCombination(map: Map<*, *>): NameCombination {
        @Suppress("UNCHECKED_CAST")
        return NameCombination(
            surHanjaStroke = map["sur_hanja_stroke"] as Int,
            stroke1 = map["stroke1"] as Int,
            stroke2 = map["stroke2"] as Int,
            fourTypes = map["four_types"] as List<Int>,
            fourTypesLuck = map["four_types_luck"] as List<Int>,
            initialScore = map["initial_score"] as Int,
            namePn = map["name_pn"] as List<Int>,
            namePnSum = map["name_pn_sum"] as Int,
            nameElements = map["name_elements"] as List<Int>,
            nameElementChecks = (map["name_element_checks"] as List<Map<String, Any>>).map {
                ElementCheck(
                    position = it["position"] as String,
                    elements = it["elements"] as String,
                    diff = it["diff"] as Int,
                    result = it["result"] as String
                )
            },
            scoreCoexistName = map["score_coexist_name"] as Int,
            typeElements = map["type_elements"] as List<Int>,
            typeElementChecks = (map["type_element_checks"] as List<Map<String, Any>>).map {
                ElementCheck(
                    position = it["position"] as String,
                    elements = it["elements"] as String,
                    diff = it["diff"] as Int,
                    result = it["result"] as String
                )
            },
            scoreCoexistType = map["score_coexist_type"] as Int,
            finalScore = map["final_score"] as Int,
            scoreZeroReason = map["score_zero_reason"] as? String
        )
    }
}