// model/domain/service/search/hanja/HanjaSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import com.ssc.namespring.model.data.mapper.HanjaInfo

internal interface HanjaSearchStrategy {
    fun search(query: String): List<HanjaInfo>
}