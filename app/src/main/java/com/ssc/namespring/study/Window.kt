package com.ssc.namespring.study

import android.view.View

class Window {
    // 1. 뷰 (레이아웃, 뷰)에 대한 간단 설명
    // 2. 즉흥적 샘플 구현
    // 3. 요구사항 추출
    // 4. UI 스케치 시작

    // 실제 설계 및 구현 시작 (드디어 앱개발시작단계)
    // ...
    // 앱 완성?
    // 앱 마켓 등록
    // 끝!
    // +@ ? 업데이트로 인앱광고같은거 가능. gpt

    // 1. 뷰
    // 뷰 클래스의 구조
    // 뷰: 화면을 구성하는 메인 요소.
    // 자기 참조 구조체 적 형태
    open class WindowMenu {
        var text: String = "게임 >"
        var child: WindowMenu? = null // (스타크래프트)

        fun onMouseHover() {
            // DB를 읽어서 데이터를 가져왔다 치자.
            // child를 동적 생성해주자.
            // 1. if (child != null) {...} 가 싫다!
//            child?.let {
//                // it // non-null로 들어온다.
//                it.text = "스타크래프트"
//                it.open()
//            }
            // 가독성은 좋은데말이야.. 소스코드 수준에서의 코드가 더 간결하고 더 예뻤으면 좋겠다!
            // 2. let말고 it 생략해보자. 왜냐? child가 난널인건 이미 다 알텐데 굳이 it?
            child?.apply {
                text = "스타크래프트" // this.는 늘 생략할 수 있다는 건 보편적 언어 코딩 방식.
                // this@Window.text = "게임(오픈됨) >"
                open()
            } // {} 스코프 내 child가 디폴트 this가 된다.
            // 너무 간결하게 하다보니까 누구의 변수(함수)인지 그 주체를 모르게된다.
        }

        fun open() {

        }
    }

    var view: View? = null // View도 이렇게 자기참조구조체로 구성되어 있어요.

    // ViewGroup: N개의 뷰들을 가질 수 있는 뷰.
    class WindowMenuGroup : WindowMenu() {
        var menus: List<WindowMenu> = ArrayList<WindowMenu>()
    }
}