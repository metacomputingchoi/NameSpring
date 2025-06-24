// model/application/service/filter/impl/LengthFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.constants.Constants

class LengthFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedHanja = name.hanja1Info.hanja + name.hanja2Info.hanja
        val combinedPronounciation = "${name.hanja1Info.inmyeongYongEum}${name.hanja2Info.inmyeongYongEum}"

        if (combinedPronounciation.length != Constants.NAME_LENGTH ||
            combinedHanja.length != Constants.NAME_LENGTH) {
            return FilterResult(
                passed = false,
                reason = "combined_pronounciation_length=${combinedPronounciation.length}, combined_hanja_length=${combinedHanja.length}"
            )
        }

        // 성공 시 결합된 값들을 저장
        name.combinedHanja = combinedHanja
        name.combinedPronounciation = combinedPronounciation

        return FilterResult(passed = true)
    }

    override fun getFilterName() = "length_check"
}