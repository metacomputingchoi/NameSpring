// model/domain/service/interfaces/IProfileDependencyProvider.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.usecase.ProfileUseCase

interface IProfileDependencyProvider {
    fun provideProfileUseCase(): ProfileUseCase
}