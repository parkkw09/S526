package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.peter.s526.R
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.presentation.view.main.MainViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    isbn: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var detail by remember { mutableStateOf<BookDetail?>(null) }
    var isBookmarked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isbn) {
        viewModel.getDetailBook(isbn) { result ->
            detail = result
            isBookmarked = viewModel.isBookmarked(result)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior,
                // Expanded height 에 이미지를 배치
                expandedHeight = 278.dp,
                actions = {
                    // 빈 액션 영역
                }
            )
        },
        floatingActionButton = {
            detail?.let { bookDetail ->
                FloatingActionButton(
                    onClick = {
                        isBookmarked = if (isBookmarked) {
                            viewModel.deleteBookmark(bookDetail)
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.delete_bookmark)) }
                            false
                        } else {
                            viewModel.addBookmark(bookDetail)
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.add_bookmark)) }
                            true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = if (isBookmarked) "Delete Bookmark" else "Add Bookmark"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 배경 이미지를 TopAppBar의 영역에 맞춰 그리기 위해 Box 로 감쌉니다.
            // LargeTopAppBar 의 title 영역 밖에서 이미지를 그리면 CollapsingToolbar 효과를 낼 수 있습니다.
            detail?.let { bookDetail ->
                AsyncImage(
                    model = bookDetail.image,
                    contentDescription = null,
                    placeholder = painterResource(id = R.drawable.book),
                    error = painterResource(id = R.drawable.book),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(278.dp) // AppBar 의 expandedHeight 와 동일하게 설정
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = bookDetail.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (bookDetail.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bookDetail.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val authorPublisher = buildString {
                        if (bookDetail.authors.isNotBlank()) append(bookDetail.authors)
                        if (bookDetail.publisher.isNotBlank()) {
                            if (isNotEmpty()) append(" / ")
                            append(bookDetail.publisher)
                        }
                    }
                    if (authorPublisher.isNotBlank()) {
                        Text(
                            text = authorPublisher,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val langIsbn = listOfNotNull(
                        bookDetail.language.takeIf { it.isNotBlank() },
                        bookDetail.isbn13.takeIf { it.isNotBlank() },
                        bookDetail.isbn10.takeIf { it.isNotBlank() },
                    ).joinToString(" / ")
                    
                    if (langIsbn.isNotBlank()) {
                        Text(
                            text = langIsbn,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val info = listOfNotNull(
                        bookDetail.pages.takeIf { it.isNotBlank() && it != "0" }?.let { "$it pages" },
                        bookDetail.year.takeIf { it.isNotBlank() }
                    ).joinToString(" / ")

                    if (info.isNotBlank()) {
                        Text(
                            text = info,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SelectionContainer {
                        Text(
                            text = bookDetail.desc,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = bookDetail.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // FAB 공간
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
