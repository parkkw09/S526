package app.peter.s526.presentation.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import app.peter.s526.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S526TopBar(
    appName: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onReviewClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            Row {
                onReviewClick?.let { onClick ->
                    IconButton(onClick = onClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.review),
                            contentDescription = "Review",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                onSearchClick?.let { onClick ->
                    IconButton(onClick = onClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
