// view/impl/SplashViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.SplashView
import kotlinx.coroutines.delay

class SplashViewImpl(private val activity: Activity) : SplashView {

    private val logger = AndroidLogger("SplashView")

    override fun showSplashAnimation() {
        logger.d("🌱 이름봄 앱 시작")
        logger.d("🌱 새싹이 자라나는 중...")
        logger.d("🌿 새싹이 무럭무럭...")
        logger.d("🌳 나무로 성장 중...")
        logger.d("🌸 꽃이 피었습니다!")
    }

    override fun navigateToMain() {
        logger.d("메인 화면으로 이동합니다")
    }
}