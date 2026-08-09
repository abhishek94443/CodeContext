package com.codecontext.cli

import com.codecontext.core.ai.AICodeAnalyzer
import com.codecontext.core.ai.CodebaseContext
import com.codecontext.core.config.ConfigLoader
import com.codecontext.core.graph.RobustDependencyGraph
import com.codecontext.core.scanner.RepositoryScanner
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import java.io.File
import kotlinx.coroutines.runBlocking

class AIAssistantCommand :
        CliktCommand(name = "ask", help = "Ask questions about your codebase using AI") {
    private val question by argument("question", help = "The question to ask").default("")

    override fun run() {
        if (question.isBlank()) {
            throw com.github.ajalt.clikt.core.PrintHelpMessage(currentContext)
        }

        val config = ConfigLoader.load()

        if (!config.ai.enabled || config.ai.apiKey.isEmpty()) {
            echo(
                    "❌ AI features disabled. Please enable them in .codecontext.json and set output.ai.apiKey"
            )
            return
        }

        echo("🤖 Analyzing codebase to answer: \"$question\"")

        runBlocking {
            // Quick Scan & Parse
            echo("   Gathering context...")
            val root = File(".")
            val scanner = RepositoryScanner()
            val files = scanner.scan(root.absolutePath)

            // Re-instantiate cache manager or load config-based one
            val cacheManager = com.codecontext.core.cache.CacheManager()
            val parallelParser = CodeParallelParser(cacheManager)
            val parsedFiles: List<com.codecontext.core.parser.ParsedFile> =
                    parallelParser.parseFiles(files)

            // Build Graph for Hotspots
            val graph = RobustDependencyGraph()
            graph.build(parsedFiles)
            graph.analyze() // PageRank

            val hotspots = graph.getTopHotspots(10).map { it.first }

            val context =
                    CodebaseContext(
                            totalFiles = parsedFiles.size,
                            languages = listOf("Kotlin/Java"),
                            hotspots = hotspots,
                            recentChanges = emptyList()
                    )

            try {
                val aiAnalyzer =
                        AICodeAnalyzer(config.ai.apiKey, config.ai.model, config.ai.provider)
                val response = aiAnalyzer.askQuestion(question, context)

                echo("\n💡 ${response.answer}\n")

                if (response.suggestedFiles.isNotEmpty()) {
                    echo("📁 Check these files:")
                    response.suggestedFiles.forEach { echo("   - $it") }
                }

                echo("\n🎯 Confidence: ${(response.confidence * 100).toInt()}%")
            } catch (e: Exception) {
                if (e is com.codecontext.core.exceptions.CodeContextException) throw e
                throw com.codecontext.core.exceptions.AIProviderException("Failed to get AI response", e)
            }
        }
    }
}
