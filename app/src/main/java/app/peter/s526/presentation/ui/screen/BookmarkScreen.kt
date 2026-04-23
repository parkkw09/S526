package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.peter.s526.R
import app.peter.s526.domain.model.Book
import app.peter.s526.presentation.ui.component.BookCard
import app.peter.s526.presentation.view.main.MainViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkScreen(
    viewModel: MainViewModel,
    onBookClick: (Book) -> Unit
) {
    val bookmarkFlow by viewModel.bookmark.collectAsStateWithLifecycle()
    var localBookmark by remember { mutableStateOf(bookmarkFlow) }

    LaunchedEffect(bookmarkFlow) {
        localBookmark = bookmarkFlow
    }

    LaunchedEffect(Unit) {
        viewModel.getBookmark()
    }

    // 화면을 벗어날 때(onDestroyView 대체) 변경된 순서 저장
    DisposableEffect(Unit) {
        onDispose {
            viewModel.updateBookmark(localBookmark)
        }
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState
    ) { from, to ->
        localBookmark = localBookmark.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { localBookmark = localBookmark.sortedBy { it.title } },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.ascending))
            }
            Button(onClick = { localBookmark = localBookmark.sortedByDescending { it.title } }) {
                Text(text = stringResource(id = R.string.descending))
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = localBookmark,
                key = { _, book -> book.isbn }
            ) { index, book ->
                ReorderableItem(
                    state = reorderableState,
                    key = book.isbn
                ) { isDragging ->
                    val modifier = Modifier.longPressDraggableHandle()
                    // 스와이프 삭제 기능(SwipeToDismissBox)은 리스크 관리를 위해 별도로 추가할 수 있음
                    // (현재는 드래그 앤 드롭 정렬만 구현)
                    BookCard(
                        book = book,
                        index = index,
                        onClick = onBookClick,
                        modifier = modifier
                    )
                }
            }
        }
    }
}
