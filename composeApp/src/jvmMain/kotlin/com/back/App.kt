package com.back

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 현재 선택된 탭. 기본으로 가장 먼저 보이는 탭(수업 메모)이 선택된다. (requirements.md 2.1)
        var selectedTab by remember { mutableStateOf(MemoTab.DEFAULT) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "메모 앱",
                    style = MaterialTheme.typography.titleLarge,
                )
                // 선택된 탭의 메모 내용이 표시되는 영역.
                // 탭 전환 UI와 블록 편집은 이후 기능에서 구현한다.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = selectedTab.label,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
