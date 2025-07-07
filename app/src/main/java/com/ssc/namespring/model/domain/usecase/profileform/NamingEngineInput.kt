// model/domain/usecase/profileform/NamingEngineInput.kt
package com.ssc.namespring.model.domain.usecase.profileform

import java.time.LocalDateTime

data class NamingEngineInput(
    val userInput: String,
    val birthDateTime: LocalDateTime,
    val useYajasi: Boolean
)