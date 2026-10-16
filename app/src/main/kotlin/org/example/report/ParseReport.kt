package org.example.report

import org.example.model.RawProduct

data class ParseReport(
    val parsed: List<RawProduct>,
    val errors: List<RowError>,
)
