package org.example.export

import kotlinx.serialization.json.Json
import org.example.model.Product
import java.io.File

class JsonProductExporter : Exporter {
    override val format = "JSON"

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun export(products: List<Product>, outputPath: String) {
        val content = buildJson(products)
        save(content, outputPath)
    }

    private fun buildJson(products: List<Product>): String =
        json.encodeToString(products)

    private fun save(content: String, outputPath: String) {
        File(outputPath).writeText(content, Charsets.UTF_8)
    }
}
