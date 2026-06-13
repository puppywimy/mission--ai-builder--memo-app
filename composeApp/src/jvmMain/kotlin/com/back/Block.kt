package com.back

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 메모를 구성하는 최소 단위 블록. (requirements.md 4, 5)
 *
 * 각 탭은 블록들로 이루어진 하나의 메모 문서처럼 동작하며, 블록은 작성한 순서대로
 * 위에서 아래로 표시된다. 제목·리스트·코드 등 블록 서식은 이후 기능에서 추가하며,
 * 현재는 일반 텍스트만 보관한다. (requirements.md 6)
 */
class Block(text: String = "") {
    /**
     * 블록을 안정적으로 식별하기 위한 고유 id.
     * 블록 추가·삭제·순서 변경 시에도 목록에서 각 블록을 구분하는 데 사용된다.
     */
    val id: Long = nextId()

    /** 블록에 입력된 텍스트. 입력 시 화면이 갱신되도록 Compose 상태로 보관한다. */
    var text: String by mutableStateOf(text)

    private companion object {
        private var lastId = 0L
        private fun nextId(): Long = ++lastId
    }
}
