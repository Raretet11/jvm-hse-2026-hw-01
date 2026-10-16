package org.example.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExporterFactoryTest {
    @Test fun createsExporterForEveryFormat() {
        ExportFormat.entries.forEach { format ->
            assertEquals(format.name, ExporterFactory.create(format).format)
        }
    }

    @Test fun createsExporterMatchingFormat() {
        assertTrue(ExporterFactory.create(ExportFormat.JSON) is JsonProductExporter)
        assertTrue(ExporterFactory.create(ExportFormat.XML) is XmlProductExporter)
        assertTrue(ExporterFactory.create(ExportFormat.TXT) is TxtReportExporter)
    }
}