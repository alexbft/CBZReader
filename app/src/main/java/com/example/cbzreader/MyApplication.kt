package com.example.cbzreader

import android.app.Application
import com.example.cbzreader.data.AppPreferencesRepository
import com.example.cbzreader.data.BookPageRepository
import com.example.cbzreader.data.BookRepository

class MyApplication : Application() {
    val bookRepository: BookRepository by lazy {
        BookRepository(this)
    }

    val bookPageRepository: BookPageRepository by lazy {
        BookPageRepository(bookRepository)
    }

    val appPreferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepository(preferencesDataStore)
    }
}