package com.example.cbzreader.ui.data

import android.os.Parcelable
import com.example.cbzreader.data.Book
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookViewUiState(
    val book: Book,
    val pageIndex: Int,
    val numPages: Int
) : Parcelable
