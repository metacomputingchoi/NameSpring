// controller/NamingController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.FavoriteModel
import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namespring.model.data.FilterMode
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.DynamicFilterInput
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.LoggingHelper
import com.ssc.namespring.utils.UiHelper
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingResultView
import com.ssc.namespring.view.NamingSettingsView
import com.ssc.namespring.view.impl.NamingSettingsViewImpl
import kotlinx.coroutines.*
import java.time.LocalDateTime

/**
 * 작명 기능 컨트롤러
 *
 * 역할:
 * - 작명 설정 및 결과 처리
 * - 필터 검증 및 이름 생성
 * - 즐겨찾기 관리
 * - 계절/시간대별 보너스 적용
 */
class NamingController(
    private val nameGeneratorModel: NameGeneratorModel,
    private val favoriteModel: FavoriteModel,
    private val namingSettingsView: NamingSettingsView,
    private val namingResultView: NamingResultView
) {

    private val logger = AndroidLogger("NamingController")

    // 컨트롤러 전용 코루틴 스코프
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentProfile: Profile? = null
    private var generatedNames: List<GeneratedName> = emptyList()
    private var currentSortType = SortType.SCORE
    private var specialConsideration: String? = null // 특별 상황 (쌍둥이, 입양 등)

    // 작명 완료 콜백
    var onNamingCompleted: (() -> Unit)? = null

    /**
     * 작명 설정 화면 표시
     */
    suspend fun showNamingSettings(profile: Profile) {
        currentProfile = profile

        // 프로필 기반 동적 필터 입력 초기화
        val dynamicFilter = DynamicFilterInput.fromProfile(profile)

        namingSettingsView.showDynamicFilterInput()
        namingSettingsView.showFilterModeToggle()
        namingSettingsView.enableGenerateButton(true)

        // 특별 상황 체크 (프로필명으로 간단히 판단)
        checkSpecialConsiderations(profile)

        // 계절별 행운 요소 표시
        showSeasonalLuckElements()

        // 시간대별 특별 메시지
        showTimeBasedMessage()

        // 프로필별 맞춤 작명 팁
        LoggingHelper.logNamingTips(profile)

        // 테스트용 필터 설정 (프로필 정보 포함)
        (namingSettingsView as? NamingSettingsViewImpl)?.setTestFilter(
            surname = profile.surname,
            surnameHanja = profile.surnameHanja,
            profile = profile
        )

        // 자동으로 생성 시작 (테스트)
        handleFilteredNaming()
    }

    /**
     * 특별 상황 체크
     */
    private fun checkSpecialConsiderations(profile: Profile) {
        when {
            profile.profileName.contains("쌍둥이") ||
                    profile.profileName.contains("둘째") -> {
                specialConsideration = "twins"
                showSpecialConsiderationTips("twins")
            }
            profile.profileName.contains("입양") -> {
                specialConsideration = "adoption"
                showSpecialConsiderationTips("adoption")
            }
        }
    }

    /**
     * 특별 상황에 대한 팁 표시
     */
    private fun showSpecialConsiderationTips(situation: String) {
        JsonLoader.getSpecialConsiderationTips(situation)?.let { tips ->
            logger.d("")
            logger.d("【${tips.title}】")
            tips.tips?.forEach { tip ->
                logger.d("• $tip")
            }
        }
    }

    /**
     * 계절별 행운 요소 표시
     */
    private fun showSeasonalLuckElements() {
        val month = LocalDateTime.now().monthValue
        JsonLoader.getSeasonalMessage(month)?.let { seasonal ->
            logger.d("")
            logger.d("🌿 ${seasonal.message}")
            logger.d("   이 계절의 행운 오행: ${seasonal.luckyElements.joinToString(", ")}")
            logger.d("   ${seasonal.specialBonus}")
        }
    }

    /**
     * 시간대별 메시지 표시
     */
    private fun showTimeBasedMessage() {
        val hour = LocalDateTime.now().hour
        JsonLoader.getTimeBasedMessage(hour)?.let { message ->
            logger.d("")
            logger.d("⏰ $message")
        }
    }

    /**
     * 필터 설정에 따른 작명 실행
     * UI에서 '생성' 버튼 클릭 시 호출
     */
    suspend fun handleFilteredNaming() {
        val filter = namingSettingsView.getFilterSettings()

        // 필터 설명 표시
        namingSettingsView.showFilterDescription(filter.getFilterDescription())

        // 발음 관련 팁 표시
        if (filter.filterMode == FilterMode.DETAIL) {
            showPronunciationTips()
        }

        // 유효성 검증
        if (!namingSettingsView.validateFilters()) {
            return
        }

        namingSettingsView.showLoading(true)
        namingSettingsView.enableGenerateButton(false)
        namingResultView.showLoading(true)

        try {
            val profile = currentProfile ?: return
            val nameInput = filter.toInputString()

            // 입력 유효성 검증
            val validationResult = nameGeneratorModel.validateInput(nameInput)
            if (!validationResult.isValid) {
                namingResultView.showError("입력 오류: ${validationResult.errorMessage}")
                return
            }

            val startTime = System.currentTimeMillis()

            // 이름 생성 (NameGeneratorModel 사용)
            generatedNames = withContext(Dispatchers.IO) {
                nameGeneratorModel.generateNames(
                    userInput = nameInput,
                    birthDateTime = profile.birthDateTime,
                    useYajasi = profile.useYajasi,
                    verbose = false,
                    withoutFilter = filter.filterMode == FilterMode.SIMPLE
                )
            }

            val elapsedTime = System.currentTimeMillis() - startTime

            // 생성 결과 통계 로깅
            LoggingHelper.logNameGenerationResult(generatedNames, profile, elapsedTime)

            // 계절별 보너스 점수 적용 (시각적 표시용)
            applySeasonalBonus(generatedNames)

            if (generatedNames.isEmpty()) {
                namingResultView.showEmptyResult()
                showEmptyResultGuidance()
            } else {
                // 정렬 적용
                val sortedNames = sortNames(generatedNames, currentSortType)

                // 첫 페이지 표시 (10개)
                val firstPageNames = sortedNames.take(10)
                namingResultView.showNameCards(firstPageNames)
                namingResultView.showSortOptions()

                // 결과 요약 정보
                namingResultView.showResultSummary(generatedNames.size, firstPageNames.size)

                // 페이지네이션 및 더보기
                val totalPages = (generatedNames.size + 9) / 10
                namingResultView.showPagination(1, totalPages)
                namingResultView.showLoadMore(generatedNames.size > 10)

                // 즐겨찾기 상태 표시
                updateFavoriteStates(firstPageNames, profile.id)

                // 마일스톤 체크
                checkMilestones()

                // 테스트: 첫 번째 이름을 즐겨찾기에 추가
                simulateFavoriteSelection(sortedNames.first())
            }

        } catch (e: Exception) {
            logger.e("Name generation failed", e)
            namingResultView.showError("이름 생성 실패: ${e.message}")
            showErrorGuidance(e.message ?: "알 수 없는 오류")
        } finally {
            namingSettingsView.showLoading(false)
            namingSettingsView.enableGenerateButton(true)
            namingResultView.showLoading(false)
        }
    }

    /**
     * 발음 관련 팁 표시
     */
    private fun showPronunciationTips() {
        val tips = JsonLoader.namingTips.pronunciationTips
        logger.d("")
        logger.d("【${tips.title}】")
        tips.tips?.take(2)?.forEach { tip ->
            logger.d("💡 $tip")
        }
    }

    /**
     * 계절별 보너스 적용 (시각적 표시용)
     */
    private fun applySeasonalBonus(names: List<GeneratedName>) {
        val month = LocalDateTime.now().monthValue
        JsonLoader.getSeasonalMessage(month)?.let { seasonal ->
            // 계절의 행운 오행을 가진 이름들 표시
            val luckyNames = names.filter { name ->
                name.analysisInfo?.ohaengInfo?.let { ohaeng ->
                    seasonal.luckyElements.any { element ->
                        ohaeng.baleumOhaeng.contains(element) ||
                                ohaeng.jawonOhaeng.contains(element)
                    }
                } ?: false
            }

            if (luckyNames.isNotEmpty()) {
                logger.d("")
                logger.d("🍀 이 계절의 행운 이름: ${luckyNames.size}개")
            }
        }
    }

    /**
     * 빈 결과에 대한 안내
     */
    private fun showEmptyResultGuidance() {
        // 피해야 할 획수 때문인지 체크
        val avoidNumbers = JsonLoader.getAvoidStrokeNumbers()
        logger.d("")
        logger.d("💡 조건 완화 제안:")
        logger.d("• 초성 조건을 줄여보세요")
        logger.d("• 한자 조건을 빈칸으로 바꿔보세요")
        logger.d("• 간편 모드를 사용해보세요")

        // 의미 조합 팁도 제공
        val meaningTips = JsonLoader.getMeaningCombinationTips()
        logger.d("")
        logger.d("【${meaningTips.title}】")
        meaningTips.avoidCombinations.forEach { tip ->
            logger.d("• $tip")
        }
    }

    /**
     * 에러 상황에 대한 안내
     */
    private fun showErrorGuidance(errorMessage: String) {
        val errorType = when {
            errorMessage.contains("입력") -> "input_errors"
            errorMessage.contains("생성") -> "system_errors"
            else -> "system_errors"
        }

        JsonLoader.userGuideStrings.errorMessages[errorType]?.forEach { (key, message) ->
            if (errorMessage.contains(key)) {
                logger.d("💡 $message")
            }
        }
    }

    /**
     * 마일스톤 체크
     */
    private fun checkMilestones() {
        // 첫 생성
        if (generatedNames.isNotEmpty()) {
            JsonLoader.getMilestoneMessage("first_generation")?.let { message ->
                logger.d("")
                logger.d("🎯 $message")
            }
        }

        // 고득점 이름 발견
        val highScoreNames = generatedNames.filter {
            UiHelper.getNamebomScore(it) >= 90
        }
        if (highScoreNames.isNotEmpty()) {
            JsonLoader.getMilestoneMessage("first_90_score")?.let { message ->
                logger.d("")
                logger.d("🏆 $message")
            }
        }
    }

    /**
     * 테스트용 즐겨찾기 선택 시뮬레이션
     */
    private suspend fun simulateFavoriteSelection(name: GeneratedName) {
        logger.d("")
        logger.d("=== 즐겨찾기 시뮬레이션 ===")
        logger.d("첫 번째 이름을 즐겨찾기에 추가합니다...")

        handleNameFavorite(name)

        // 잠시 대기 후 완료 콜백 호출
        delay(2000)
        onNamingCompleted?.invoke()
    }

    /**
     * 간편 모드로 전환
     */
    suspend fun handleSimpleNaming() {
        namingSettingsView.showSimpleMode()
        handleFilteredNaming()
    }

    /**
     * 이름 즐겨찾기 토글
     * UI에서 새싹 버튼 클릭 시 호출
     */
    suspend fun handleNameFavorite(name: GeneratedName) {
        val profile = currentProfile ?: return

        try {
            val result = favoriteModel.toggleFavorite(profile.id, name)

            result.onSuccess { added ->
                namingResultView.updateFavoriteStatus(name, added)
                if (added) {
                    logger.d("✅ 즐겨찾기 추가: ${name.surnameHangul}${name.combinedPronounciation}")
                }
            }.onFailure { error ->
                namingResultView.showError("즐겨찾기 처리 실패: ${error.message}")
            }

        } catch (e: Exception) {
            logger.e("Favorite toggle failed", e)
            namingResultView.showError("오류 발생: ${e.message}")
        }
    }

    /**
     * 정렬 방식 변경
     * UI에서 정렬 옵션 선택 시 호출
     */
    fun handleSortChange(sortType: SortType) {
        currentSortType = sortType
        val sortedNames = sortNames(generatedNames, sortType)
        namingResultView.showNameCards(sortedNames)
    }

    /**
     * 페이지 변경
     */
    suspend fun handlePageChange(page: Int) {
        val pageSize = 10
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, generatedNames.size)

        val pageNames = generatedNames.subList(startIndex, endIndex)
        namingResultView.showNameCards(pageNames)

        currentProfile?.let { profile ->
            updateFavoriteStates(pageNames, profile.id)
        }
    }

    /**
     * 즐겨찾기 상태 업데이트
     */
    private suspend fun updateFavoriteStates(names: List<GeneratedName>, profileId: String) {
        names.forEach { name ->
            val isFavorite = favoriteModel.isFavorite(profileId, name)
            namingResultView.showFavoriteButton(name, isFavorite)
        }
    }

    /**
     * 이름 정렬
     */
    private fun sortNames(names: List<GeneratedName>, sortType: SortType): List<GeneratedName> {
        return when (sortType) {
            SortType.SCORE -> names.sortedByDescending {
                it.analysisInfo?.totalScore ?: 0
            }
            SortType.ALPHABETICAL -> names.sortedBy {
                it.combinedPronounciation
            }
        }
    }

    enum class SortType {
        SCORE,       // 점수순
        ALPHABETICAL // 가나다순
    }
}