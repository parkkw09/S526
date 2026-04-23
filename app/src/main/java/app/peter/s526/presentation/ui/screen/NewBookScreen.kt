package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.peter.s526.domain.model.Book
import app.peter.s526.presentation.ui.component.BookCard
import app.peter.s526.presentation.view.main.MainViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val AUTO_LOAD_THRESHOLD = 5

@Composable
fun NewBookScreen(
    viewModel: MainViewModel,
    onBookClick: (Book) -> Unit
) {
    val bookList by viewModel.bookList.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Load initial data
    LaunchedEffect(Unit) {
        if (bookList.isEmpty()) {
            viewModel.getNewBook()
        }
    }

    // Infinite scroll listener
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            totalItems > 0 && lastVisibleItem >= totalItems - AUTO_LOAD_THRESHOLD
        }
        .distinctUntilChanged()
        .filter { it } // Only emit when true (reached threshold)
        .collect {
            viewModel.getNextNewBook()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = bookList,
            key = { _, book -> book.isbn }
        ) { index, book ->
            BookCard(
                book = book,
                index = index,
                onClick = onBookClick
            )
        }
    }
}
