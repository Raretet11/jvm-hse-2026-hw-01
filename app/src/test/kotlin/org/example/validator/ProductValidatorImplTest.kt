package org.example.validator

import org.example.model.Currency
import org.example.model.ProductType
import org.example.model.RawProduct
import org.example.model.VehicleType
import org.example.model.WeightUnit
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductValidatorImplTest {
    @Test fun acceptsValidProduct() {
        val report = ProductValidatorImpl.validateAll(listOf(rawProduct()))

        assertEquals(1, report.valid.size)
        assertTrue(report.errors.isEmpty())
        assertEquals("Mobil 1, Synthetic", report.valid.single().name)
    }

    @Test fun rejectsEmptyName() {
        val report = ProductValidatorImpl.validateAll(listOf(rawProduct(name = "")))

        assertTrue(report.valid.isEmpty())
        assertTrue(report.errors.single().reason.contains("Пустое название товара"))
    }

    @Test fun rejectsNegativePrice() {
        val report = ProductValidatorImpl.validateAll(
            listOf(rawProduct(priceAmount = BigDecimal("-5"), priceCurrency = Currency.USD))
        )

        assertTrue(report.valid.isEmpty())
        assertTrue(report.errors.single().reason.contains("Цена"))
    }

    @Test fun rejectsZeroWeight() {
        val report = ProductValidatorImpl.validateAll(listOf(rawProduct(weightAmount = BigDecimal("0"))))

        assertTrue(report.valid.isEmpty())
        assertTrue(report.errors.single().reason.contains("Вес"))
    }

    @Test fun rejectsNotAllowedColor() {
        val report = ProductValidatorImpl.validateAll(
            listOf(rawProduct(extra = mapOf("material" to "пластик", "color" to "red")))
        )

        assertTrue(report.valid.isEmpty())
        assertTrue(report.errors.single().reason.contains("Недопустимый цвет"))
    }

    @Test fun reportsLineNumberOfInvalidRow() {
        val report = ProductValidatorImpl.validateAll(
            listOf(
                rawProduct(lineNumber = 3),
                rawProduct(lineNumber = 9, extra = mapOf("material" to "пластик", "color" to "red")),
            )
        )

        assertEquals(1, report.valid.size)
        assertEquals(9, report.errors.single().lineNumber)
    }

    private fun rawProduct(
        lineNumber: Int = 1,
        id: Long = 1,
        name: String = "Mobil 1, Synthetic",
        priceAmount: BigDecimal = BigDecimal("12.5"),
        priceCurrency: Currency = Currency.USD,
        weightAmount: BigDecimal = BigDecimal("10"),
        extra: Map<String, String> = mapOf("volume" to "4"),
    ): RawProduct = RawProduct(
        lineNumber = lineNumber,
        id = id,
        name = name,
        type = ProductType.OIL,
        priceAmount = priceAmount,
        priceCurrency = priceCurrency,
        weightAmount = weightAmount,
        weightUnit = WeightUnit.LB,
        vehicle = VehicleType.PASSENGER,
        extra = extra,
    )
}