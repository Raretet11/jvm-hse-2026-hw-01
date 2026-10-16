package org.example.report

import org.example.model.Product

data class ValidationReport(
    val valid: List<Product>,
    val errors: List<RowError>,
)
