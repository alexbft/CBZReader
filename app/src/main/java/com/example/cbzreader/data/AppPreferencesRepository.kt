package com.example.cbzreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

private val recentBookNameKey = stringPreferencesKey("recentBookName")
private val recentBookSizeKey = longPreferencesKey("recentBookSize")
private val recentBookUniqueIdKey = stringPreferencesKey("recentBookUniqueId")
private val recentPageListKey = stringPreferencesKey("recentPageList")

private const val maxRecentEntries = 20

class AppPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    suspend fun getRecentBookmark(): Bookmark? {
        val preferences = dataStore.data.first()
        val recentBookName = preferences[recentBookNameKey]
        val recentBookSize = preferences[recentBookSizeKey]
        val recentBookUniqueId = preferences[recentBookUniqueIdKey]
        val recentPageList = decodePageList(preferences[recentPageListKey])
        if (recentBookName == null || recentBookSize == null || recentBookUniqueId == null) {
            return null
        }
        val recentPage =
            if (recentPageList.isNotEmpty() && recentPageList.first().first == recentBookUniqueId) {
                recentPageList.first().second
            } else 0
        val book = Book(
            displayName = recentBookName,
            fileSize = recentBookSize,
            uniqueId = recentBookUniqueId
        )
        return Bookmark(book, recentPage)
    }

    suspend fun updateRecentBookmark(bookmark: Bookmark) {
        dataStore.edit { preferences ->
            val book = bookmark.book
            preferences[recentBookNameKey] = book.displayName
            preferences[recentBookUniqueIdKey] = book.uniqueId
            preferences[recentBookSizeKey] = book.fileSize
            preferences[recentPageListKey] =
                updatePageList(preferences[recentPageListKey], book.uniqueId, bookmark.page)
        }
    }

    suspend fun getRecentPageForBook(bookUniqueId: String): Int {
        val preferences = dataStore.data.first()
        val pageList = decodePageList(preferences[recentPageListKey])
        return pageList.firstOrNull { it.first == bookUniqueId }?.second ?: 0
    }

    suspend fun clearRecentBookmark() {
        dataStore.edit { preferences ->
            preferences.remove(recentBookNameKey)
            preferences.remove(recentBookUniqueIdKey)
            preferences.remove(recentBookSizeKey)
        }
    }

    private fun decodePageList(prefString: String?): List<Pair<String, Int>> {
        if (prefString == null) return emptyList()
        return prefString.split(",").mapNotNull { encPair ->
            val parts = encPair.split(":", limit = 2)
            if (parts.size != 2) {
                return@mapNotNull null
            }
            val uniqueId = parts[0]
            val page = parts[1].toIntOrNull() ?: return@mapNotNull null
            uniqueId to page
        }
    }

    private fun encodePageList(pageList: List<Pair<String, Int>>): String {
        return pageList.joinToString(",") { "${it.first}:${it.second}" }
    }

    private fun updatePageList(prefString: String?, uniqueId: String, page: Int): String {
        var pageList = decodePageList(prefString)
        pageList = pageList.filter { it.first != uniqueId }
        pageList = listOf(uniqueId to page) + pageList
        if (pageList.size > maxRecentEntries) {
            pageList = pageList.subList(0, maxRecentEntries)
        }
        return encodePageList(pageList)
    }
}