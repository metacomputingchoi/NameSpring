// model/domain/service/search/hanja/IHanjaSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import com.ssc.namespring.model.data.mapper.HanjaInfo

internal interface IHanjaSearchStrategy {
    fun search(query: String): List<HanjaInfo>
}