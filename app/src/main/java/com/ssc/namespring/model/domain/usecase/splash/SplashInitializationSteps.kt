// model/domain/usecase/splash/SplashInitializationSteps.kt
package com.ssc.namespring.model.domain.usecase.splash

data class InitializationStep(
    val startProgress: Int,
    val endProgress: Int,
    val message: String,
    val action: suspend () -> Unit
)

object SplashInitializationSteps {
    const val PROFILE_MANAGER_PROGRESS = 10
    const val TASK_MANAGER_PROGRESS = 15
    const val JSON_LOADER_PROGRESS = 20
    const val NAMING_ENGINE_START_PROGRESS = 30
    const val NAMING_ENGINE_END_PROGRESS = 40
    const val DATA_LOADER_START_PROGRESS = 50
    const val DATA_LOADER_END_PROGRESS = 90
    const val FINAL_VERIFICATION_PROGRESS = 95
    const val COMPLETE_PROGRESS = 100
}