package org.example.export

import org.example.model.Currency
import org.example.model.Price
import org.example.model.Product
import org.example.model.ProductType
import org.example.model.VehicleType
import org.example.model.Weight
import org.example.model.WeightUnit
import org.junit.jupiter.api.io.TempDir
import org.w3c.dom.Element
import java.math.BigDecimal
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlProductExporterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test fun exportsProductsAsXml() {
        XmlProductExporter().export(listOf(product()), tempDir.resolve("output.xml").toString())

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(tempDir.resolve("output.xml").toFile())

        assertEquals("products", document.documentElement.nodeName)

        val products = document.getElementsByTagName("product")
        assertEquals(1, products.length)

        val product = products.item(0) as Element
        assertEquals("1", product.childText("id"))
        assertEquals("Mobil 1, Synthetic", product.childText("name"))
        assertEquals("oil", product.childText("type"))
        assertEquals("PASSENGER", product.childText("vehicle"))

        val price = product.getElementsByTagName("price").item(0) as Element
        assertEquals("1125.00000", price.textContent)
        assertEquals("RUB", price.getAttribute("currency"))

        val weight = product.getElementsByTagName("weight").item(0) as Element
        assertEquals("4.54000", weight.textContent)
        assertEquals("KG", weight.getAttribute("unit"))

        val params = product.getElementsByTagName("param")
        assertEquals(1, params.length)
        assertEquals("volume", (params.item(0) as Element).getAttribute("key"))
        assertEquals("4", params.item(0).textContent)
    }

    private fun Element.childText(tag: String): String = getElementsByTagName(tag).item(0).textContent

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