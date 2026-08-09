package com.codecontext.core.cache

import com.codecontext.core.parser.ParsedFile
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CacheManager(private val cacheDir: File = File(".codecontext/cache")) {

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }

    // FIX: Add thread-safe locking per file
    private val locks = ConcurrentHashMap<String, ReentrantReadWriteLock>()

    private fun getLock(key: String): ReentrantReadWriteLock {
        return locks.computeIfAbsent(key) { ReentrantReadWriteLock() }
    }

    fun getCachedParse(file: File): ParsedFile? {
        val cacheKey = getCacheKey(file)
        val cacheFile = File(cacheDir, "$cacheKey.json")

        val lock = getLock(cacheKey)

        return lock.read {
            if (!cacheFile.exists()) return@read null

            // Check if source file is newer than cache
            if (file.lastModified() > cacheFile.lastModified()) return@read null

            try {
                val json = cacheFile.readText()
                Json.decodeFromString<ParsedFile>(json)
            } catch (e: Exception) {
                // If cache is corrupted, delete it
                cacheFile.delete()
                null
            }
        }
    }

    fun saveParse(file: File, parsed: ParsedFile) {
        val cacheKey = getCacheKey(file)
        val cacheFile = File(cacheDir, "$cacheKey.json")

        val lock = getLock(cacheKey)

        lock.write {
            try {
                val json = Json.encodeToString(parsed)
                // FIX: Atomic write using temp file
                val tempFile = File(cacheFile.absolutePath + ".tmp")
                tempFile.writeText(json)
                tempFile.renameTo(cacheFile) // Atomic on most filesystems
            } catch (e: Exception) {
                System.err.println("Failed to cache ${file.name}: ${e.message}")
            }
        }
    }

    private fun getCacheKey(file: File): String {
        // FIX: Include file size and last modified in key for better invalidation
        val path = file.absolutePath
        val metadata = "${path}:${file.lastModified()}:${file.length()}"
        return MessageDigest.getInstance("MD5").digest(metadata.toByteArray()).joinToString("") {
            "%02x".format(it)
        }
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
        locks.clear()
    }
}
