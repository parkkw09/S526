package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.peter.s526.R
import app.peter.s526.domain.model.Book
import app.peter.s526.presentation.ui.component.S526TopBar
import app.peter.s526.presentation.view.main.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    viewModel: MainViewModel,
    onBookClick: (Book) -> Unit,
    onSearchClick: () -> Unit,
    onHistoryQueryClick: (String) -> Unit
) {
    val tabs = listOf(
        Pair(stringResource(id = R.string.new_book), R.drawable.book),
        Pair(stringResource(id = R.string.bookmark), R.drawable.bookmark),
        Pair(stringResource(id = R.string.history), R.drawable.history)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            S526TopBar(
                appName = viewModel.appName ?: stringResource(id = R.string.app_name),
                onReviewClick = { /* Activity 레벨에서 처리 필요 (InAppReview) */ },
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, (title, iconRes) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title) },
                        icon = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = title
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> NewBookScreen(viewModel, onBookClick)
                    1 -> BookmarkScreen(viewModel, onBookClick)
                    2 -> HistoryScreen(viewModel, onHistoryQueryClick)
                }
            }
        }
    }
}
