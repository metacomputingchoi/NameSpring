// model/domain/service/search/hanja/HanjaResultConverter.kt
package com.ssc.namespring.model.domain.service.search.hanja

import com.ssc.namespring.model.data.mapper.HanjaInfo
import com.ssc.namespring.model.domain.entity.HanjaSearchResult

internal class HanjaResultConverter {

    fun convert(info: HanjaInfo): HanjaSearchResult {
        return HanjaSearchResult(
            korean = info.korean,
            hanja = info.hanja,
            meaning = info.meaning,
            ohaeng = info.ohaeng,
            strokes = info.strokes,
            soundCount = 1,
            tripleKey = info.tripleKey
        )
    }
}