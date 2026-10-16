package org.example.report

data class RowError(
    val lineNumber: Int,
    val row: List<String>,
    val reason: String,
) {
    override fun toString(): String = "[ОШИБКА] Строка $lineNumber: $reason"
}
