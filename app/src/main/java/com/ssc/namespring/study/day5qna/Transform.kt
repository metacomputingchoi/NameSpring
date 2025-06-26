package com.ssc.namespring.study.day5qna

// 3차원 기하 변환해주는 클래스를 짠다고 치자
class Transform(
    v: Vector3
) {
    data class Vector3(
        var x: Int,
        var y: Int,
        var z: Int
    )
}
// 철학 1. 개발자 왈: "나는 Vector3 조차도 외부 라이브러리 사용자가 알아야 하는 API 레벨이다!"
// -> 이 경우에는 별도 클래스로 나와야 함.

// 철학 2. 개발자 왈: "나는 Vector3는 오로지 Transform 클래스만을 위한거야! 딴데서는 안씀!"
// -> inner class로 구현

// 결론은 "의도"다.
// (항상 우현이가 강조하는것: 흐름과 의도)

