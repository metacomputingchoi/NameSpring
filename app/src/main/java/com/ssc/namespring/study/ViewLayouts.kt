package com.ssc.namespring.study

class ViewLayouts {

    // 1. Layouts(=ViewGroup)
    // 뷰들의 집합이자 동시에 뷰 그 자체다.
    // LinearLayout : 이미 해봤음
    // FrameLayout : 완전 자유롭게 배치. 뷰들에 대한 좌표를 수기 지정..
    // GridLayout : NxM의 매트릭스 형태로 순차 배치, 각 셀마다 가중치 값이 있어서 각 뷰들이 서로다른 가중치로 자유롭게 배치됨
    // ScrollView : 스크롤 가능한 타입으로 뷰 자식을 여러개 가질 때 스크롤 가능
    // Relative한 layout 계열..
    // ..
    // 더 많지만 안드로이드도 다 잘 만들어놓지 않았음. 어떤건 deprecated되기도 하고 그럼.

    // * 개발자의 무의식의 흐름
    // 1. 애니메이션 되는 뷰 처리가 필요.
    // 2. FrameLayout을 채택하고 수동으로 제어? (DFS)
    // 3. BFS적으로 생각해보면, 우리가 모르는 다른 레이아웃이 이미 구현해 놓은게 있지 않을까?
    // 4. 없어? 그럼 어쩔수 없이 2번 DPS

    // 2. View
    // 화면의 단위 구성 요소
    // TextView: 텍스트 표시할 수 있는 놈
    // EditText: 텍스트 표시할 수 있는 놈. 유저편집 OK
    // Button: 텍스트 표시할 수 있는 놈. 클릭 가능한놈(사실 모든 뷰는 clickable)
    // ImageButton: 이미지를 표시하는 버튼Add commentMore actions
    // RecyclerView: 문자적으로는 재활용뷰. (예시설명 구두로 설명 추가함.) 그런데 어떤 뷰그룹 내에서 동적 생성삭제가 되는 뷰
    // CheckBox, ProgressBar, ComboBox, ... 등 우리가 일반적인 UI 프로그래밍 할 때 필요한 요소들은 어지간하면 다 찾아보면 있다!
}