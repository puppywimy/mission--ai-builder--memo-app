package com.back

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 현재 선택된 탭. 기본으로 가장 먼저 보이는 탭(수업 메모)이 선택된다. (requirements.md 2.1)
        var selectedTab by remember { mutableStateOf(MemoTab.DEFAULT) }

        // 탭별 메모 블록 목록. 각 탭은 블록들로 구성된 하나의 메모 문서처럼 동작하며(requirements.md 4, 5),
        // 탭을 전환해도 각 탭에 작성 중이던 블록 내용이 그대로 유지된다. (requirements.md 3)
        val tabBlocks = remember {
            mutableStateMapOf<MemoTab, SnapshotStateList<Block>>().apply {
                MemoTab.entries.forEach { tab -> put(tab, mutableStateListOf(Block())) }
            }
        }
        val blocks = tabBlocks.getValue(selectedTab)

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

                // 선택된 탭의 블록 기반 메모 편집 영역.
                MemoEditor(
                    blocks = blocks,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 블록 단위 메모 편집 영역. (requirements.md 4, 5)
 *
 * 세 탭은 모두 이 동일한 편집 방식을 사용하며, 각 탭은 블록들이 위에서 아래로 이어지는
 * 하나의 메모 문서처럼 동작한다. 블록 사이에는 구분선이나 테두리가 보이지 않아
 * Notion과 유사하게 전체가 하나의 문서처럼 보인다. (requirements.md 4, 5)
 */
@Composable
private fun MemoEditor(
    blocks: SnapshotStateList<Block>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 블록은 추가·삭제 시에도 안정적으로 구분되도록 id를 key로 사용한다.
        items(blocks, key = { block -> block.id }) { block ->
            BlockRow(
                block = block,
                onTextChange = { block.text = it },
                // 블록을 삭제해도 나머지 블록의 내용과 순서는 그대로 유지된다.
                onDelete = { blocks.remove(block) },
            )
        }

        item {
            // 새 블록을 메모의 맨 아래에 추가한다. (requirements.md 5)
            TextButton(onClick = { blocks.add(Block()) }) {
                Text("+ 블록 추가")
            }
        }
    }
}

/**
 * 하나의 블록을 편집하는 행.
 *
 * 블록 입력 필드는 테두리·배경·구분선이 없는 BasicTextField로 표시해, 여러 블록이
 * 모이면 전체가 하나의 메모 문서처럼 자연스럽게 이어지도록 한다. (requirements.md 4, 5)
 */
@Composable
private fun BlockRow(
    block: Block,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    // 빈 블록에 입력을 안내하는 플레이스홀더는, 해당 블록에 포커스가 있을 때만 표시한다.
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = block.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box {
                    if (block.text.isEmpty() && isFocused) {
                        Text(
                            text = "내용을 입력하세요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )
        TextButton(onClick = onDelete) {
            Text("삭제")
        }
    }
}
