package com.back

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    // Enter나 + 버튼으로 새 블록을 추가하면 사용자가 곧바로 이어서 입력할 수 있도록
    // 새 블록으로 포커스를 옮긴다. 포커스를 줄 블록의 id를 보관한다. (requirements.md 5.1)
    var focusRequestId by remember { mutableStateOf<Long?>(null) }

    // 주어진 블록 바로 아래에 새 블록을 추가하고, 새 블록으로 포커스를 옮긴다. (requirements.md 5.1)
    fun insertBelow(block: Block) {
        val index = blocks.indexOf(block)
        if (index < 0) return
        val newBlock = Block()
        blocks.add(index + 1, newBlock)
        focusRequestId = newBlock.id
    }

    // 빈 블록에서 백스페이스를 누르면 해당 블록을 삭제하고 이전 블록으로 포커스를 옮긴다.
    // 첫 블록이면 삭제하지 않아, 메모에는 항상 최소 한 개의 블록이 남는다. (requirements.md 5.3)
    fun deleteAndFocusPrevious(block: Block) {
        val index = blocks.indexOf(block)
        if (index <= 0) return
        val previous = blocks[index - 1]
        blocks.removeAt(index)
        focusRequestId = previous.id
    }

    LazyColumn(
        modifier = modifier
            // 메모의 빈 공간을 클릭하면 가장 아래 블록에 포커스를 줘 곧바로 이어서 입력할 수 있게 한다.
            // 블록 텍스트 필드·아이콘 버튼은 자식이 먼저 클릭을 소비하므로, 빈 영역 클릭에만 반응한다.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                val last = blocks.lastOrNull()
                when {
                    // 마지막 블록이 비어 있으면 그 블록에 바로 포커스를 준다.
                    last == null || last.text.isEmpty() -> last?.let { focusRequestId = it.id }
                    // 마지막 블록에 내용이 있으면 새 빈 블록을 추가하고 그 블록으로 포커스를 옮긴다.
                    else -> insertBelow(last)
                }
            },
        // 블록 사이 간격을 살짝 둬, 한 블록 안의 줄바꿈과 블록 경계가 시각적으로 구분되게 한다.
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 블록은 추가·삭제 시에도 안정적으로 구분되도록 id를 key로 사용한다.
        items(blocks, key = { block -> block.id }) { block ->
            BlockRow(
                block = block,
                onTextChange = { block.text = it },
                // Enter 또는 + 버튼: 해당 블록 바로 아래에 새 블록을 추가한다. (requirements.md 5.1)
                onInsertBelow = { insertBelow(block) },
                // 빈 블록에서 백스페이스: 블록을 삭제하고 이전 블록으로 포커스를 옮긴다. (requirements.md 5.3)
                onBackspaceWhenEmpty = { deleteAndFocusPrevious(block) },
                onDelete = {
                    // 블록을 삭제해도 나머지 블록의 내용과 순서는 그대로 유지된다. (requirements.md 5.3)
                    blocks.remove(block)
                    // 모든 블록이 사라지면 더 이상 호버할 블록이 없으므로, 빈 블록 하나를 남겨 둔다.
                    if (blocks.isEmpty()) blocks.add(Block())
                },
                requestFocus = focusRequestId == block.id,
                onFocusHandled = { focusRequestId = null },
            )
        }
    }
}

/**
 * 하나의 블록을 편집하는 행.
 *
 * 블록 입력 필드는 테두리·배경·구분선이 없는 BasicTextField로 표시해, 여러 블록이
 * 모이면 전체가 하나의 메모 문서처럼 자연스럽게 이어지도록 한다. (requirements.md 4, 5)
 *
 * 블록 좌측에 마우스를 호버하면 + 버튼과 휴지통 아이콘이 나타나, 각각 아래에 새 블록을
 * 추가하거나 해당 블록을 삭제한다. (requirements.md 5.1, 5.3)
 */
@Composable
private fun BlockRow(
    block: Block,
    onTextChange: (String) -> Unit,
    onInsertBelow: () -> Unit,
    onBackspaceWhenEmpty: () -> Unit,
    onDelete: () -> Unit,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
) {
    // 블록 행에 마우스를 호버하면 좌측 거터의 + / 휴지통 버튼을 보여 준다. (requirements.md 5.1, 5.3)
    val rowInteractionSource = remember { MutableInteractionSource() }
    val isHovered by rowInteractionSource.collectIsHoveredAsState()
    // 거터가 갑자기 나타나지 않고 부드럽게 나타나도록 alpha를 애니메이션한다.
    val gutterAlpha by animateFloatAsState(if (isHovered) 1f else 0f)

    // 빈 블록에 입력을 안내하는 플레이스홀더는, 해당 블록에 포커스가 있을 때만 표시한다.
    val fieldInteractionSource = remember { MutableInteractionSource() }
    val isFocused by fieldInteractionSource.collectIsFocusedAsState()

    // 블록 입력 상태. Shift + Enter 줄바꿈을 커서 위치에 직접 삽입하기 위해 선택 영역까지
    // 추적하는 TextFieldValue로 보관한다. (requirements.md 5.1)
    var fieldValue by remember { mutableStateOf(TextFieldValue(block.text)) }

    // 새로 추가된 블록이면 포커스를 요청해, 사용자가 곧바로 입력을 이어갈 수 있게 한다. (requirements.md 5.1)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    Row(
        modifier = Modifier.hoverable(rowInteractionSource),
        verticalAlignment = Alignment.Top,
        // + / 휴지통 거터와 텍스트 사이에 약간의 간격을 둔다.
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 좌측 거터: 호버할 때만 보이도록 alpha로 토글하되, 폭은 항상 차지해 레이아웃이 흔들리지 않게 한다.
        Row(
            modifier = Modifier.alpha(gutterAlpha),
            // + 버튼과 휴지통 아이콘 사이 간격.
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // + 버튼: 해당 블록 바로 아래에 새 블록을 추가한다. (requirements.md 5.1)
            IconButton(onClick = onInsertBelow, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "블록 추가",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // 휴지통 아이콘: 해당 블록을 삭제한다. (requirements.md 5.3)
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "블록 삭제",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onTextChange(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        // Shift + Enter: Compose Desktop 기본 키 매핑은 줄바꿈으로 처리하지 않으므로,
                        // 커서 위치(선택 영역)에 줄바꿈을 직접 삽입한다. (requirements.md 5.1)
                        event.key == Key.Enter && event.isShiftPressed -> {
                            val selection = fieldValue.selection
                            val newText = fieldValue.text.replaceRange(selection.min, selection.max, "\n")
                            val cursor = selection.min + 1
                            fieldValue = fieldValue.copy(text = newText, selection = TextRange(cursor))
                            onTextChange(newText)
                            true
                        }
                        // Enter: 기본 줄바꿈을 막고 아래에 새 블록을 추가한다. (requirements.md 5.1)
                        event.key == Key.Enter -> {
                            onInsertBelow()
                            true
                        }
                        // 빈 블록에서 백스페이스: 블록을 삭제하고 이전 블록으로 포커스를 옮긴다. (requirements.md 5.3)
                        event.key == Key.Backspace && fieldValue.text.isEmpty() -> {
                            onBackspaceWhenEmpty()
                            true
                        }
                        else -> false
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = fieldInteractionSource,
            decorationBox = { innerTextField ->
                Box {
                    if (fieldValue.text.isEmpty() && isFocused) {
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
    }
}