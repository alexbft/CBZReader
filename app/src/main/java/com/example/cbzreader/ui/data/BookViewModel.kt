package com.example.cbzreader.ui.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.cbzreader.data.AppPreferencesRepository
import com.example.cbzreader.data.Book
import com.example.cbzreader.data.BookPageRepository
import com.example.cbzreader.data.BookRepository
import com.example.cbzreader.data.Bookmark
import kotlinx.coroutines.launch

class BookViewModel(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val bookRepository: BookRepository,
    val bookPageRepository: BookPageRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    @OptIn(SavedStateHandleSaveableApi::class)
    var homeScreenUiState by savedStateHandle.saveable("ui_state") {
        mutableStateOf(HomeScreenUiState(isInitializing = true, bookViewUiState = null))
    }
        private set

    init {
        if (savedStateHandle.get<Boolean>("initialized") != true) {
            savedStateHandle["initialized"] = true
            loadBookmark()
        }
    }

    private fun loadBookmark() {
        Log.d("my", "loadBookmark")
        viewModelScope.launch {
            val bookmark = appPreferencesRepository.getRecentBookmark()
            val bookViewUiState = bookmark?.let {
                getNumPages(bookmark.book)?.let { numPages ->
                    Log.d("my", "loadBookmark ${bookmark.book} ${bookmark.page}")
                    BookViewUiState(
                        book = bookmark.book,
                        pageIndex = bookmark.page,
                        numPages = numPages,
                    )
                }
            }
            if (homeScreenUiState.isInitializing) {
                homeScreenUiState =
                    HomeScreenUiState(isInitializing = false, bookViewUiState = bookViewUiState)
            }
        }
    }

    suspend fun saveBookmark() {
        Log.d("my", "saveBookmark")
        homeScreenUiState.bookViewUiState?.also {
            appPreferencesRepository.updateRecentBookmark(
                Bookmark(book = it.book, page = it.pageIndex)
            )
            Log.d("my", "saveBookmark ${it.book} ${it.pageIndex}")
        } ?: appPreferencesRepository.clearRecentBookmark()
    }

    fun openBook(uri: Uri) {
        viewModelScope.launch {
            homeScreenUiState = homeScreenUiState.copy(isInitializing = true)
            val book = bookRepository.getBookByUri(uri)
            val bookViewUiState = book?.let {
                getNumPages(book)?.let { numPages ->
                    BookViewUiState(
                        book = book,
                        pageIndex = appPreferencesRepository.getRecentPageForBook(book.uniqueId),
                        numPages = numPages
                    )
                }
            }
            homeScreenUiState =
                HomeScreenUiState(isInitializing = false, bookViewUiState = bookViewUiState)
        }
    }

    fun closeBook() {
        viewModelScope.launch {
            saveBookmark()
            homeScreenUiState = homeScreenUiState.copy(bookViewUiState = null)
        }
    }

    fun changePage(page: Int) {
        homeScreenUiState = homeScreenUiState.copy(
            bookViewUiState = homeScreenUiState.bookViewUiState?.copy(
                pageIndex = page
            )
        )
    }

    private suspend fun getNumPages(book: Book): Int? {
        return bookPageRepository.getBookIndex(book)?.pages?.size
    }
}