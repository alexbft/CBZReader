package com.example.cbzreader.data

import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.zip.ZipFile

class PageCache(val book: Book, val bookIndex: BookIndex, val zipFile: ZipFile) : Closeable {
    private var isClosed = false

    init {
        Log.d("my", "created PageCache for $book")
    }

    override fun close() {
        zipFile.close()
        isClosed = true
    }

    suspend fun getPage(index: Int): Page? {
        if (isClosed) {
            throw IllegalStateException("PageCache is closed")
        }
        return loadPage(index)
    }

    private suspend fun loadPage(index: Int): Page? {
        val pageInfo = bookIndex.pages[index] ?: return null
        return withContext(Dispatchers.IO) {
            zipFile.getEntry(pageInfo.entryPath)?.let { zipEntry ->
                zipFile.getInputStream(zipEntry).let { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap?.let {
                        Page(pageInfo, it)
                    }
                }
            }
        }
    }
}