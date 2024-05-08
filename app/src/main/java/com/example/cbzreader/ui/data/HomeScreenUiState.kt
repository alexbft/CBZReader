package com.example.cbzreader.ui.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeScreenUiState(
    val isInitializing: Boolean,
    val bookViewUiState: BookViewUiState?
) : Parcelable
