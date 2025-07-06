// model/domain/service/factory/NameDataServiceFactory.kt
package com.ssc.namespring.model.domain.service.factory

import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.name.NameDataServiceImpl
import com.ssc.namespring.model.data.repository.NameDataRepositoryImpl

/**
 * NameDataService 인스턴스 생성을 담당하는 팩토리
 * 향후 의존성 주입 프레임워크로 대체 가능
 */
object NameDataServiceFactory {

    @Volatile
    private var instance: INameDataService? = null

    /**
     * NameDataService의 싱글톤 인스턴스를 반환
     * @return INameDataService 인스턴스
     */
    fun getInstance(): INameDataService {
        return instance ?: synchronized(this) {
            instance ?: createInstance().also { instance = it }
        }
    }

    /**
     * 새로운 NameDataService 인스턴스 생성
     * @return INameDataService 인스턴스
     */
    fun createInstance(): INameDataService {
        val repository = NameDataRepositoryImpl()
        return NameDataServiceImpl(repository)
    }

    /**
     * 테스트용: 커스텀 서비스 인스턴스 설정
     * @param service 설정할 서비스 인스턴스
     */
    internal fun setInstance(service: INameDataService) {
        synchronized(this) {
            instance = service
        }
    }

    /**
     * 테스트용: 인스턴스 초기화
     */
    internal fun reset() {
        synchronized(this) {
            instance = null
        }
    }
}