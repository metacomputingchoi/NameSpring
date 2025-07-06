// model/domain/service/interfaces/INameDataService.kt
package com.ssc.namespring.model.domain.service.interfaces

import android.content.Context
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.data.mapper.MappingStats

/**
 * 이름 데이터 서비스 인터페이스
 * 한자 검색 및 이름 데이터 관리 기능을 제공
 */
interface INameDataService {
    /**
     * 서비스 초기화
     * @param context Android Context
     * @throws IllegalStateException 초기화 실패 시
     */
    fun init(context: Context)

    /**
     * 모든 한자 목록 조회
     * @return 전체 한자 검색 결과 목록
     */
    fun getAllHanja(): List<HanjaSearchResult>

    /**
     * 한자 검색
     * @param query 검색어 (초성, 한글, 한자, 뜻, 획수 등)
     * @return 검색 결과 목록
     */
    fun searchHanja(query: String): List<HanjaSearchResult>

    /**
     * 특정 triple key로 문자 정보 조회
     * @param tripleKey 문자 고유 키
     * @return 문자 정보 또는 null
     */
    fun getCharInfo(tripleKey: String): CharTripleInfo?

    /**
     * 한글과 한자로 문자 정보 조회
     * @param korean 한글
     * @param hanja 한자
     * @return 문자 정보 또는 null
     */
    fun getCharInfo(korean: String, hanja: String): CharTripleInfo?

    /**
     * 데이터 유효성 검증
     * @return 검증 결과
     */
    fun validateData(): ValidationResult

    /**
     * 뜻으로 한자 검색
     * @param query 검색할 뜻
     * @return 검색 결과 목록
     */
    fun searchHanjaByMeaning(query: String): List<HanjaSearchResult>

    /**
     * 한자 모양으로 검색
     * @param query 검색할 한자
     * @return 검색 결과 목록
     */
    fun searchHanjaByHanja(query: String): List<HanjaSearchResult>

    /**
     * 서비스 준비 상태 확인
     * @return 준비 완료 여부
     */
    fun isReady(): Boolean

    /**
     * 통계 정보 조회
     * @return 매핑 통계 또는 null
     */
    fun getStats(): MappingStats?
}