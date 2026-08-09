package com.codecontext.core.scanner

import java.io.File

class RepositoryScanner {
    fun scan(rootPath: String): List<File> {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            throw IllegalArgumentException("Invalid repository path: $rootPath")
        }

        return root.walkTopDown()
                .filter { it.isFile }
                .filter { file ->
                    val name = file.name
                    (name.endsWith(".kt") || name.endsWith(".java")) &&
                            !file.absolutePath.contains("${File.separator}.git${File.separator}") &&
                            !file.absolutePath.contains(
                                    "${File.separator}.idea${File.separator}"
                            ) &&
                            !file.absolutePath.contains(
                                    "${File.separator}.gradle${File.separator}"
                            ) &&
                            !file.absolutePath.contains(
                                    "${File.separator}build${File.separator}"
                            ) &&
                            !file.absolutePath.contains(
                                    "${File.separator}target${File.separator}"
                            ) &&
                            !file.absolutePath.contains(
                                    "${File.separator}node_modules${File.separator}"
                            )
                }
                .toList()
    }
}
