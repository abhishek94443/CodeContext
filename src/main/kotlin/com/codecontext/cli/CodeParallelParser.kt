package com.codecontext.cli

import com.codecontext.core.cache.CacheManager
import com.codecontext.core.parser.ParsedFile
import com.codecontext.core.parser.ParserFactory
import java.io.File
import kotlinx.coroutines.*

class CodeParallelParser(private val cacheManager: CacheManager? = null) {

    suspend fun parseFiles(files: List<File>): List<ParsedFile> = coroutineScope {
        // FIX: Dynamic chunk size based on available memory
        val runtime = Runtime.getRuntime()
        val availableMemory = runtime.freeMemory()
        val totalMemory = runtime.totalMemory()
        val usedMemory = totalMemory - availableMemory

        // Use smaller chunks if memory is tight
        val chunkSize =
                when {
                    availableMemory < 256_000_000 -> 25 // Low memory
                    availableMemory < 512_000_000 -> 50 // Medium memory
                    else -> 100 // High memory
                }

        println(
                "💾 Memory: ${usedMemory / 1_000_000}MB used, ${availableMemory / 1_000_000}MB free"
        )
        println("📦 Using chunk size: $chunkSize")

        // FIX: Add progress tracking
        var processed = 0
        val total = files.size

        files.chunked(chunkSize).flatMap { chunk ->
            chunk
                    .map { file ->
                        async(Dispatchers.IO) {
                            try {
                                // Check cache first
                                cacheManager?.getCachedParse(file)?.let {
                                    processed++
                                    if (processed % 100 == 0) {
                                        println("   Progress: $processed/$total files")
                                    }
                                    return@async it
                                }

                                // Parse if not cached
                                val parser = ParserFactory.getParser(file)
                                val parsed = parser.parse(file)

                                // Save to cache
                                cacheManager?.saveParse(file, parsed)

                                processed++
                                if (processed % 100 == 0) {
                                    println("   Progress: $processed/$total files")
                                }

                                parsed
                            } catch (e: Exception) {
                                // FIX: Don't crash on single file failure
                                println("⚠️  Failed to parse ${file.name}: ${e.message}")
                                null
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull() // FIX: Remove failed files
        }
    }
}
