package org.example.export

import org.example.model.Product

interface Exporter {
    val format: String

    fun export(products: List<Product>, outputPath: String)
}
