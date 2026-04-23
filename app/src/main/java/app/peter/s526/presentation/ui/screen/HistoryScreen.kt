package app.peter.s526.presentation.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.peter.s526.presentation.ui.component.HistoryCard
import app.peter.s526.presentation.view.main.MainViewModel

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onQueryClick: (String) -> Unit
) {
    val historyList by viewModel.history.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getHistory()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = historyList,
            key = { query -> query }
        ) { query ->
            HistoryCard(
                query = query,
                onClick = onQueryClick
            )
        }
    }
}
