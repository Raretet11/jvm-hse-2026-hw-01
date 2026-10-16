package org.example

import org.example.export.ExportFormat
import org.example.export.ExporterFactory
import org.example.normalizer.ProductNormalizer
import org.example.parser.ProductParser
import org.example.reader.Reader
import org.example.report.ParseReport
import org.example.report.ValidationReport
import org.example.validator.ProductValidator

class Pipeline(
    val reader: Reader,
    val exportFilename: String,
    val exportFormat: ExportFormat,
    val parser: ProductParser,
    val validator: ProductValidator,
    val normalizer: ProductNormalizer
) {
    fun run() {
        val rows = reader.readAll()
        val parseReport = parser.parseAll(rows)
        val validationReport = validator.validateAll(parseReport.parsed)
        val normalizedRows = normalizer.normalizeAll(validationReport.valid)

        val exporter = ExporterFactory.create(exportFormat)
        exporter.export(normalizedRows, exportFilename)

        printStat(rows.size, parseReport, validationReport)
    }

    private fun printStat(total: Int, parseReport: ParseReport, validationReport: ValidationReport) {
        println("Обработано строк: $total \nУспешно загружено товаров: ${validationReport.valid.size} \n" +
                    "Ошибок парсинга: ${parseReport.errors.size} \nОшибок валидации: ${validationReport.errors.size} \n")

        println("ОШИБКИ ПАРСИНГА:")
        parseReport.errors.forEach { println(it) }

        println("\nОШИБКИ ВАЛИДАЦИИ:")
        validationReport.errors.forEach { println(it) }

    }
}
