package com.back

/**
 * 앱은 수업 중 떠오르는 내용을 세 가지로 분류해 기록한다. (requirements.md 1, 2)
 *
 * 표시 순서는 선언 순서를 따르며, 첫 번째 탭(수업 메모)이 기본으로 보인다.
 */
enum class MemoTab(val label: String) {
    CLASS_NOTES("수업 메모"),
    QUESTIONS("질문"),
    QUICK_NOTES("빠른 메모"),
    ;

    companion object {
        /** 기본으로 가장 먼저 보이는 탭. */
        val DEFAULT: MemoTab = CLASS_NOTES
    }
}