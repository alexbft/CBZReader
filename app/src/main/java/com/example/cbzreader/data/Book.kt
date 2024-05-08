package com.example.cbzreader.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val displayName: String,
    val fileSize: Long,
    val uniqueId: String,
) : Parcelable
