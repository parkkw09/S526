package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.peter.s526.R
import app.peter.s526.domain.model.Book
import app.peter.s526.presentation.ui.component.BookCard
import app.peter.s526.presentation.ui.component.S526TopBar
import app.peter.s526.presentation.view.main.MainViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val AUTO_LOAD_THRESHOLD = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    initialQuery: String,
    onBookClick: (Book) -> Unit,
    onBackClick: () -> Unit
) {
    val searchResults by viewModel.searchBookList.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }

    // Paging State (기존 Fragment 에 있던 상태를 여기서 관리)
    var currentPage by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            isLoading = true
            currentPage = 1
            isComplete = false
            viewModel.searchBook(initialQuery, "1") { page, total ->
                currentPage = page
                isComplete = searchResults.size >= total
                isLoading = false
            }
        } else {
            viewModel.clearSearchResult()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            totalItems > 0 && lastVisibleItem >= totalItems - AUTO_LOAD_THRESHOLD
        }
        .distinctUntilChanged()
        .filter { it && !isLoading && !isComplete && searchQuery.isNotBlank() }
        .collect {
            isLoading = true
            viewModel.searchBook(searchQuery, (currentPage + 1).toString()) { page, total ->
                currentPage = page
                isComplete = searchResults.size >= total
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            S526TopBar(
                appName = viewModel.appName ?: stringResource(id = R.string.app_name),
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    active = false
                    if (it.isNotBlank()) {
                        isLoading = true
                        currentPage = 1
                        isComplete = false
                        viewModel.addHistory(it)
                        viewModel.searchBook(it, "1") { page, total ->
                            currentPage = page
                            isComplete = searchResults.size >= total
                            isLoading = false
                        }
                    }
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = stringResource(id = R.string.search_hint)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                // SearchBar 확장 시 검색 기록 등을 보여줄 수 있음
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = searchResults,
                        key = { _, book -> book.isbn }
                    ) { index, book ->
                        BookCard(
                            book = book,
                            index = index,
                            onClick = onBookClick
                        )
                    }
                }
                
                if (isLoading && searchResults.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
