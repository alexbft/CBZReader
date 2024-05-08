package com.example.cbzreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cbzreader.data.BookPageRepository
import com.example.cbzreader.ui.data.HomeScreenUiState

@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    bookPageRepository: BookPageRepository,
    fullScreenMode: Boolean,
    onOpenBookClicked: () -> Unit,
    onPageChange: (Int) -> Unit,
    onCloseBook: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isInitializing) {
            InitializationSplash()
        } else {
            if (uiState.bookViewUiState == null) {
                EmptyBookView(onOpenBookClicked = onOpenBookClicked)
            } else {
                BookView(
                    uiState = uiState.bookViewUiState,
                    bookPageRepository = bookPageRepository,
                    fullScreenMode = fullScreenMode,
                    onPageChange = onPageChange,
                    onCloseBook = onCloseBook,
                    onToggleFullscreen = onToggleFullscreen,
                )
            }
        }
    }
}