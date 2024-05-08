package com.example.cbzreader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cbzreader.data.BookPageRepository
import com.example.cbzreader.ui.data.BookViewUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookView(
    uiState: BookViewUiState,
    bookPageRepository: BookPageRepository,
    fullScreenMode: Boolean,
    onPageChange: (Int) -> Unit,
    onCloseBook: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.pageIndex,
        pageCount = { uiState.numPages }
    )
    val scope = rememberCoroutineScope()
    val sliderState = remember {
        SliderState(
            value = uiState.pageIndex.toFloat(),
            steps = uiState.numPages,
            valueRange = 0f..(uiState.numPages - 1).toFloat()
        )
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            sliderState.value = page.toFloat()
            onPageChange(page)
        }
    }
    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.value }.collect { sliderValue ->
            val newPage = sliderValue.roundToInt()
            if (newPage != pagerState.currentPage) {
                // Log.d("my", "scroll to $newPage")
                pagerState.scrollToPage(newPage)
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .let {
                    if (!fullScreenMode) it.safeDrawingPadding() else it
                }
        ) {
            HorizontalPager(
                state = pagerState,
                outOfBoundsPageCount = 1,
            ) { pageIndex ->
                PageView(
                    book = uiState.book,
                    pageIndex = pageIndex,
                    bookPageRepository = bookPageRepository,
                    onGoForward = {
                        scope.launch {
                            if (!pagerState.isScrollInProgress) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    onGoBack = {
                        scope.launch {
                            if (!pagerState.isScrollInProgress) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onToggleFullscreen = {
                        if (!pagerState.isScrollInProgress) {
                            onToggleFullscreen()
                        }
                    },
                )
            }
            if (!fullScreenMode) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { /*TODO*/ }) {
                            Text(text = "${uiState.pageIndex + 1}/${uiState.numPages}")
                        }
                        IconButton(
                            onClick = onCloseBook,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close book",
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Slider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 48.dp),
                        state = sliderState,
                    )
                }
            }
        }
    }
}