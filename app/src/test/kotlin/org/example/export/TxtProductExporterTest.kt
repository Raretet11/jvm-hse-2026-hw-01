package org.example.export

import org.example.model.Currency
import org.example.model.Price
import org.example.model.Product
import org.example.model.ProductType
import org.example.model.VehicleType
import org.example.model.Weight
import org.example.model.WeightUnit
import org.junit.jupiter.api.io.TempDir
import java.math.BigDecimal
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class TxtReportExporterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test fun exportsTextReport() {
        TxtReportExporter().export(listOf(product()), tempDir.resolve("output.txt").toString())

        val content = tempDir.resolve("output.txt").toFile().readText(Charsets.UTF_8)

        assertTrue(content.contains("1. Mobil 1, Synthetic (id=1)"))
        assertTrue(content.contains("type: oil"))
        assertTrue(content.contains("price: 1125.00000 RUB"))
        assertTrue(content.contains("weight: 4.54000 KG"))
        assertTrue(content.contains("vehicle: PASSENGER"))
        assertTrue(content.contains("extra: volume=4"))
    }

    @Test fun skipsExtraLineWhenNoExtra() {
        val empty = product().copy(extra = emptyMap())
        TxtReportExporter().export(listOf(empty), tempDir.resolve("output.txt").toString())

        val content = tempDir.resolve("output.txt").toFile().readText(Charsets.UTF_8)

        assertTrue(!content.contains("extra:"))
    }

    private fun product(): Product = Product(
        id = 1,
        name = "Mobil 1, Synthetic",
        type = ProductType.OIL,
        price = Price(BigDecimal("1125.00000"), Currency.RUB),
        weight = Weight(BigDecimal("4.54000"), WeightUnit.KG),
        vehicle = VehicleType.PASSENGER,
        extra = mapOf("volume" to "4"),
    )
}