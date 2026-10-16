package org.example.parser

import org.example.report.ParseReport

interface ProductParser {
    fun parseAll(rows: List<List<String>>): ParseReport
}
