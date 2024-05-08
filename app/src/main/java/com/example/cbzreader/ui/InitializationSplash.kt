package com.example.cbzreader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun InitializationSplash() {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}