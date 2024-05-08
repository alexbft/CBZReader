package com.example.cbzreader

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cbzreader.ui.Root
import com.example.cbzreader.ui.data.BookViewModel
import kotlinx.coroutines.runBlocking

val Context.preferencesDataStore by preferencesDataStore("settings")

class MainActivity : ComponentActivity() {
    private val application
        get() = applicationContext as MyApplication

    private var fullScreenMode by mutableStateOf(false)

    private val bookViewModel by viewModels<BookViewModel> {
        viewModelFactory {
            initializer {
                BookViewModel(
                    appPreferencesRepository = application.appPreferencesRepository,
                    bookPageRepository = application.bookPageRepository,
                    bookRepository = application.bookRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Root(
                bookViewModel = bookViewModel,
                fullScreenMode = fullScreenMode,
                onToggleFullscreen = {
                    toggleFullscreen()
                }
            )
        }
    }

    private fun toggleFullscreen() {
        fullScreenMode = !fullScreenMode
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        if (fullScreenMode) {
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            bookViewModel.saveBookmark()
        }
        application.bookPageRepository.clearCache()
    }
}
