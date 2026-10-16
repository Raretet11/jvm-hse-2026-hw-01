package org.example.reader

import java.io.File

class CsvReader(
    private val path: String = "input.csv",
    private val delimiter: Char = ',',
    private val hasHeader: Boolean = true
) : Reader {
    private val file: File = File(path)
    var header: List<String>? = null
        private set

    init {
        require(file.exists()) { "$path не найден" }
        require(file.isFile) { "$path не является файлом" }
    }

    override fun readAll(): List<List<String>> {
        val result = mutableListOf<List<String>>()
        file.forEachLine { line ->
            if (line.isNotBlank()) {
                result.add(parseLine(line))
            }
        }
        if (hasHeader && result.isNotEmpty()) {
            header = result.first()
            return result.drop(1)
        }
        return result
    }

    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }

        result.add(current.toString())
        return result
    }
}