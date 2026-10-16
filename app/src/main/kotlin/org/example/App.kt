package org.example

import org.example.export.ExportFormat
import org.example.normalizer.ProductNormalizerImpl
import org.example.parser.ProductParserImpl
import org.example.reader.CsvReader
import org.example.validator.ProductValidatorImpl

fun main() {
    print("Выберите название файла отчета: ")
    val exportFormatName = readln()

    var exportFormat = ExportFormat.TXT
    var exportFormatTxt: String
    var formatIncorrect = true

    while (formatIncorrect) {
        print("Выберите формат отчета (TXT, JSON, XML): ")
        exportFormatTxt = readln()
        try {
            exportFormat = ExportFormat.valueOf(exportFormatTxt)
            formatIncorrect = false
        } catch (_: IllegalArgumentException) {
            println("Формат $exportFormatTxt не поддерживается")
        }
    }

    val pipeline = Pipeline(
        CsvReader(),
        exportFormatName,
        exportFormat,
        ProductParserImpl,
        ProductValidatorImpl,
        ProductNormalizerImpl
    )
    pipeline.run()
}
