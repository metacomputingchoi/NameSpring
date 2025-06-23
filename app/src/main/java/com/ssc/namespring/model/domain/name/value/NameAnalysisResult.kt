// model/domain/name/value/NameAnalysisResult.k
package com.ssc.namespring.model.domain.name.value

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance

data class NameAnalysisResult(
    val names: List<Name>,
    val saju: Saju?,
    val elementBalance: ElementBalance?,
    val request: NameAnalysisRequest
)