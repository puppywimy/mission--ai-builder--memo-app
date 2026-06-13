package com.back

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 현재 선택된 탭. 기본으로 가장 먼저 보이는 탭(수업 메모)이 선택된다. (requirements.md 2.1)
        var selectedTab by remember { mutableStateOf(MemoTab.DEFAULT) }

        // 탭별 메모 내용. 탭을 전환해도 각 탭에 작성 중이던 내용이 유지되도록
        // 탭마다 독립적으로 내용을 보관한다. (requirements.md 3)
        // 블록 기반 편집은 이후 기능에서 구현하며, 현재는 자유 텍스트로 보관한다.
        val tabContents = remember { mutableStateMapOf<MemoTab, String>() }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "메모 앱",
                    style = MaterialTheme.typography.titleLarge,
                )

                // 세 가지 탭을 빠르게 전환할 수 있는 탭 바. (requirements.md 3)
                TabRow(selectedTabIndex = MemoTab.entries.indexOf(selectedTab)) {
                    MemoTab.entries.forEach { tab ->
                        Tab(
                            selected = tab == selectedTab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label) },
                        )
                    }
                }

                // 선택된 탭의 메모 내용. 탭 전환 시에도 내용이 사라지지 않고 유지된다.
                OutlinedTextField(
                    value = tabContents[selectedTab].orEmpty(),
                    onValueChange = { tabContents[selectedTab] = it },
                    modifier = Modifier.fillMaxSize(),
                    label = { Text(selectedTab.label) },
                )
            }
        }
    }
}
