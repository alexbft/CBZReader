package com.example.cbzreader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.cbzreader.data.Book
import com.example.cbzreader.data.BookPageRepository
import com.example.cbzreader.data.Page
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun PageView(
    book: Book,
    pageIndex: Int,
    bookPageRepository: BookPageRepository,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    var isLoading by remember {
        mutableStateOf(true)
    }
    var page by remember {
        mutableStateOf<Page?>(null)
    }
    LaunchedEffect(book, pageIndex, bookPageRepository) {
        isLoading = true
        page = bookPageRepository.getPage(book, pageIndex)
        // Log.d("my", "Loaded page: $pageIndex $page")
        isLoading = false
    }
    val zoomState = rememberZoomState(contentSize = page?.bitmap?.let {
        Size(it.width.toFloat(), it.height.toFloat())
    } ?: Size.Zero)
    if (isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.zoomable(zoomState)) {
            CircularProgressIndicator()
        }
    } else if (page == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.zoomable(zoomState)) {
            Icon(Icons.Default.Warning, contentDescription = "Error loading page")
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            var pageWidth = 0
            Image(
                bitmap = page!!.bitmap.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { pageWidth = it.size.width }
                    .zoomable(zoomState, onTap = { position ->
                        if (pageWidth > 0 && position.x / pageWidth < 0.25) {
                            onGoBack()
                        } else if (pageWidth > 0 && position.x / pageWidth > 0.75) {
                            onGoForward()
                        } else if (pageWidth > 0) {
                            onToggleFullscreen()
                        }
                    })
            )
        }
    }
}
