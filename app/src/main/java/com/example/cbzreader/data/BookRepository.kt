package com.example.cbzreader.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.cbzreader.common.UNIQUE_ID_SECRET
import com.example.cbzreader.common.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class BookRepository(
    private val context: Context
) {
    suspend fun getBookByUri(contentUri: Uri): Book? {
        val contentInfo = getContentInfo(contentUri) ?: return null
        val book = Book(
            displayName = contentInfo.displayName,
            fileSize = contentInfo.size,
            uniqueId = calcUniqueId(contentInfo.displayName, contentInfo.size)
        )
        return try {
            ensureTempFile(book, contentUri)
            book
        } catch (e: IOException) {
            Log.e("getBookByUri", "Error when copying book to cache", e)
            null
        }
    }

    fun getZipFile(book: Book): ZipFile? {
        val tempFile = getTempFile(book)
        if (!tempFile.exists()) {
            return null
        }
        return ZipFile(tempFile)
    }

    private fun getContentInfo(uri: Uri): ContentInfo? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null
        ) ?: return null
        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }
            val displayNameColIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val displayName = cursor.getString(displayNameColIndex)
            val sizeColIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val size = cursor.getLong(sizeColIndex)
            return ContentInfo(displayName, size)
        }
    }

    private fun getTempFile(book: Book): File {
        val name = "${book.uniqueId}.cbz"
        return File(context.cacheDir, name)
    }

    private fun calcUniqueId(displayName: String, size: Long): String {
        return md5("${displayName}_${size}_$UNIQUE_ID_SECRET")
    }

    private suspend fun ensureTempFile(book: Book, contentUri: Uri) {
        withContext(Dispatchers.IO) {
            val tempFile = getTempFile(book)
            if (!tempFile.exists()) {
                val inputStream =
                    context.contentResolver.openInputStream(contentUri) ?: throw IOException(
                        "Cannot open uri for reading: $contentUri"
                    )
                inputStream.use {
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}

private data class ContentInfo(val displayName: String, val size: Long)