package com.example.cbzreader.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.example.cbzreader.ui.data.BookViewModel
import com.example.cbzreader.ui.theme.CBZReaderTheme

@Composable
fun Root(
    bookViewModel: BookViewModel,
    fullScreenMode: Boolean,
    onToggleFullscreen: () -> Unit,
) {
    val openBook = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { maybeUri ->
        maybeUri?.let {
            bookViewModel.openBook(it)
        }
    }
    val handleOpenBookClicked: () -> Unit = {
        openBook.launch(arrayOf("application/*"))
    }
    CBZReaderTheme {
        HomeScreen(
            uiState = bookViewModel.homeScreenUiState,
            bookPageRepository = bookViewModel.bookPageRepository,
            fullScreenMode = fullScreenMode,
            onOpenBookClicked = handleOpenBookClicked,
            onPageChange = { bookViewModel.changePage(it) },
            onCloseBook = { bookViewModel.closeBook() },
            onToggleFullscreen = onToggleFullscreen,
        )
    }
}