package com.example.cbzreader.data

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.zip.ZipFile
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

class BookPageRepository(private val bookRepository: BookRepository) {
    private var pageCache: PageCache? = null

    suspend fun getBookIndex(book: Book): BookIndex? {
        return bookRepository.getZipFile(book)?.use { zipFile ->
            readIndex(zipFile)
        }
    }

    private suspend fun readIndex(zipFile: ZipFile): BookIndex {
        return withContext(Dispatchers.IO) {
            val entries = zipFile.entries().asSequence()
            val pages = entries.filter { zipEntry -> !zipEntry.isDirectory }
                .mapIndexed { index, zipEntry ->
                    val displayName = Path(zipEntry.name).nameWithoutExtension
                    index to PageInfo(index, zipEntry.name, displayName)
                }.toMap()
            BookIndex(pages)
        }
    }

    suspend fun getPage(book: Book, pageIndex: Int): Page? {
        val existingPageCache = pageCache
        val currentPageCache = if (existingPageCache?.book == book) {
            existingPageCache
        } else {
            ensurePageCache(book)
        }
        return currentPageCache?.getPage(pageIndex)
    }

    private var creatingPageCache: Deferred<PageCache?>? = null
    private suspend fun ensurePageCache(book: Book): PageCache? {
        val maybeResult = creatingPageCache?.await()
        if (maybeResult?.book == book) {
            return maybeResult
        }
        return coroutineScope {
            val deferredCreation = async {
                val zipFile = bookRepository.getZipFile(book)
                val newPageCache = zipFile?.let {
                    PageCache(book = book, bookIndex = readIndex(it), zipFile = it)
                }
                pageCache?.close()
                pageCache = newPageCache
                newPageCache
            }
            creatingPageCache = deferredCreation
            deferredCreation.await().also {
                creatingPageCache = null
            }
        }
    }

    fun clearCache() {
        pageCache?.close()
        pageCache = null
    }
}