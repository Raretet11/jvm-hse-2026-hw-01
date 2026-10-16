package org.example.export

object ExporterFactory {
    private val exporters: Map<ExportFormat, Exporter> = mapOf(
        ExportFormat.JSON to JsonProductExporter(),
        ExportFormat.XML to XmlProductExporter(),
        ExportFormat.TXT to TxtReportExporter(),
    )

    fun create(format: ExportFormat): Exporter =
        exporters[format] ?: error("Не поддерживаемый формат экспорта: $format")
}
