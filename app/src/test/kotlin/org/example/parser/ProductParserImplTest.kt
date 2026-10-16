package org.example.parser

import org.example.model.Currency
import org.example.model.ProductType
import org.example.model.RawProduct
import org.example.model.VehicleType
import org.example.model.WeightUnit
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductParserImplTest {
    @Test fun parsesUsdPriceWithSymbolBeforeAmount() {
        val product = parseOnly(
            row("1", "Mobil 1, Synthetic", "oil", "\$12.5", "10lb", "PASSENGER", "viscosity=5W-30;volume=4")
        )

        assertEquals(BigDecimal("12.5"), product.priceAmount)
        assertEquals(Currency.USD, product.priceCurrency)
        assertEquals(BigDecimal("10"), product.weightAmount)
        assertEquals(WeightUnit.LB, product.weightUnit)
    }

    @Test fun parsesUsdPriceWithSymbolAfterAmount() {
        val product = parseOnly(
            row("3", "Michelin Primacy", "tire", "15\$", "0.3kg", "PASSENGER", "diameter=16;season=SUMMER")
        )

        assertEquals(BigDecimal("15"), product.priceAmount)
        assertEquals(Currency.USD, product.priceCurrency)
    }

    @Test fun parsesRubPrice() {
        val product = parseOnly(
            row("2", "Castrol Magnatec", "oil", "300.5руб", "1.2kg", "PASSENGER", "volume=1")
        )

        assertEquals(BigDecimal("300.5"), product.priceAmount)
        assertEquals(Currency.RUB, product.priceCurrency)
        assertEquals(BigDecimal("1.2"), product.weightAmount)
        assertEquals(WeightUnit.KG, product.weightUnit)
    }

    @Test fun parsesWholeRow() {
        val product = parseOnly(
            row("4", "Nokian Hakkapeliitta", "tire", "25руб", "2lb", "TRUCK", "diameter=18;season=WINTER")
        )

        assertEquals(
            RawProduct(
                lineNumber = 1,
                id = 4L,
                name = "Nokian Hakkapeliitta",
                type = ProductType.TIRE,
                priceAmount = BigDecimal("25"),
                priceCurrency = Currency.RUB,
                weightAmount = BigDecimal("2"),
                weightUnit = WeightUnit.LB,
                vehicle = VehicleType.TRUCK,
                extra = mapOf("diameter" to "18", "season" to "WINTER"),
            ),
            product,
        )
    }

    @Test fun ignoresUnknownExtraKeys() {
        val product = parseOnly(
            row("1", "Mobil 1, Synthetic", "oil", "\$12.5", "10lb", "PASSENGER", "viscosity=5W-30;volume=4")
        )

        assertEquals(mapOf("volume" to "4"), product.extra)
    }

    @Test fun keepsSourceLineNumber() {
        val report = ProductParserImpl.parseAll(
            listOf(
                row("1", "Mobil 1, Synthetic", "oil", "\$12.5", "10lb", "PASSENGER", "volume=4"),
                row("2", "Аккумулятор", "battery", "200руб", "0.4kg", "PASSENGER", "material=свинец"),
                row("3", "Castrol Magnatec", "oil", "300.5руб", "1.2kg", "PASSENGER", "volume=1"),
            )
        )

        assertEquals(listOf(1, 3), report.parsed.map { it.lineNumber })
    }

    @Test fun collectsErrorsWithLineNumbers() {
        val report = ProductParserImpl.parseAll(
            listOf(
                row("1", "Mobil 1, Synthetic", "oil", "\$12.5", "10lb", "PASSENGER", "volume=4"),
                row("12", "Аккумулятор", "battery", "200руб", "0.4kg", "PASSENGER", "material=свинец"),
                row("7", "Масло", "oil", "abc", "0.5kg", "PASSENGER", "volume=4"),
                row("11", "Масло", "oil", "100руб", "0.5kg", "PASSENGER", "volume=abc"),
            )
        )

        assertEquals(1, report.parsed.size)
        assertEquals(listOf(2, 3, 4), report.errors.map { it.lineNumber })
        assertTrue(report.errors[0].reason.contains("Неизвестный тип товара"))
        assertTrue(report.errors[1].reason.contains("Не удалось распарсить цену"))
        assertTrue(report.errors[2].reason.contains("volume должно быть числом"))
    }

    @Test fun rejectsRowWithWrongColumnCount() {
        val report = ProductParserImpl.parseAll(listOf(listOf("1", "Mobil", "oil")))

        assertTrue(report.parsed.isEmpty())
        assertTrue(report.errors.single().reason.contains("Ожидалось 7 колонок"))
    }

    private fun parseOnly(row: List<String>): RawProduct =
        ProductParserImpl.parseAll(listOf(row)).parsed.single()

    private fun row(vararg fields: String): List<String> = fields.toList()
}