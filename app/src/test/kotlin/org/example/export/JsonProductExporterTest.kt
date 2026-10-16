package org.example.export

import kotlinx.serialization.json.Json
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonProductExporterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test fun exportsProductsAsJson() {
        val products = listOf(product())
        JsonProductExporter().export(products, tempDir.resolve("output.json").toString())

        val content = tempDir.resolve("output.json").toFile().readText(Charsets.UTF_8)

        assertEquals(products, Json.decodeFromString<List<Product>>(content))
    }

    @Test fun serializesAmountAsString() {
        JsonProductExporter().export(listOf(product()), tempDir.resolve("output.json").toString())

        val content = tempDir.resolve("output.json").toFile().readText(Charsets.UTF_8)

        assertTrue(content.contains("\"amount\": \"1125.00000\""))
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