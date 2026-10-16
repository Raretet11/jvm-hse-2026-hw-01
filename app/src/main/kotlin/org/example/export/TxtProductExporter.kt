package org.example.export

import org.example.model.Product
import java.io.File

class TxtReportExporter : Exporter {
    override val format = "TXT"

    override fun export(products: List<Product>, outputPath: String) {
        val content = buildReport(products)
        save(content, outputPath)
    }

    private fun formatProduct(index: Int, product: Product): String {
        val sb = StringBuilder()
        sb.append("${index + 1}. ${product.name} (id=${product.id})\n")
        sb.append("   type: ${product.type.name.lowercase()}\n")
        sb.append("   price: ${product.price.amount} ${product.price.currency.name}\n")
        sb.append("   weight: ${product.weight.amount} ${product.weight.unit.name}\n")
        sb.append("   vehicle: ${product.vehicle.name}\n")
        if (product.extra.isNotEmpty()) {
            sb.append("   extra: ")
            sb.append(product.extra.entries.joinToString(", ") { "${it.key}=${it.value}" })
            sb.append("\n")
        }
        sb.append("\n")
        return sb.toString()
    }

    private fun buildReport(products: List<Product>): String {
        val sb = StringBuilder()
        products.forEachIndexed { i, p ->
            sb.append(formatProduct(i, p))
        }
        return sb.toString()
    }

    private fun save(content: String, outputPath: String) {
        File(outputPath).writeText(content, Charsets.UTF_8)
    }
}
