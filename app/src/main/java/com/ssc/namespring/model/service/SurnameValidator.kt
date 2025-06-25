// model/service/SurnameValidator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.util.normalizeNFC

class SurnameValidator(private val dataRepository: DataRepository) {

    fun validateSurname(surnameParts: List<Pair<String, String>>): Triple<Boolean, String?, String?> {
        // 성이 비어있는지 확인
        if (surnameParts.any { (hangul, hanja) ->
                hangul == Constants.INPUT_SEPARATOR || hanja == Constants.INPUT_SEPARATOR
            }) {
            return Triple(false, null, null)
        }

        // 성의 한글/한자 조합을 문자열로 만들기
        val surnameKey = surnameParts.joinToString("") { (h, hj) ->
            "$h${Constants.NAME_PART_SEPARATOR}$hj"
        }.normalizeNFC()

        // 유효성 검증
        if (!dataRepository.surnameHanjaPairMapping.containsKey(surnameKey)) {
            return Triple(false, null, null)
        }

        val surHangul = surnameParts.joinToString("") { it.first }.normalizeNFC()
        val surHanja = surnameParts.joinToString("") { it.second }.normalizeNFC()

        return Triple(true, surHangul, surHanja)
    }
}