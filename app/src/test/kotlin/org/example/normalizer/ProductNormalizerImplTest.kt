package org.example.normalizer

import org.example.model.Currency
import org.example.model.Price
import org.example.model.Product
import org.example.model.ProductType
import org.example.model.VehicleType
import org.example.model.Weight
import org.example.model.WeightUnit
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductNormalizerImplTest {
    @Test fun convertsUsdPriceToRub() {
        val product = normalizeOnly(product(price = Price(BigDecimal("12.5"), Currency.USD)))

        assertEquals(Price(BigDecimal("1125.00000"), Currency.RUB), product.price)
    }

    @Test fun keepsRubPriceAsIs() {
        val price = Price(BigDecimal("300.5"), Currency.RUB)

        assertEquals(price, normalizeOnly(product(price = price)).price)
    }

    @Test fun convertsPoundsToKilograms() {
        val product = normalizeOnly(product(weight = Weight(BigDecimal("10"), WeightUnit.LB)))

        assertEquals(Weight(BigDecimal("4.54000"), WeightUnit.KG), product.weight)
    }

    @Test fun keepsKilogramsAsIs() {
        val weight = Weight(BigDecimal("1.2"), WeightUnit.KG)

        assertEquals(weight, normalizeOnly(product(weight = weight)).weight)
    }

    @Test fun normalizesEveryProduct() {
        val normalized = ProductNormalizerImpl.normalizeAll(
            listOf(
                product(id = 1, price = Price(BigDecimal("12.5"), Currency.USD), weight = Weight(BigDecimal("10"), WeightUnit.LB)),
                product(id = 2, price = Price(BigDecimal("300.5"), Currency.RUB), weight = Weight(BigDecimal("1.2"), WeightUnit.KG)),
            )
        )

        assertEquals(
            listOf(1L, 2L),
            normalized.map { it.id },
        )
        assertEquals(
            listOf(
                Price(BigDecimal("1125.00000"), Currency.RUB),
                Price(BigDecimal("300.5"), Currency.RUB),
            ),
            normalized.map { it.price },
        )
        assertEquals(
            listOf(
                Weight(BigDecimal("4.54000"), WeightUnit.KG),
                Weight(BigDecimal("1.2"), WeightUnit.KG),
            ),
            normalized.map { it.weight },
        )
    }

    private fun normalizeOnly(product: Product): Product =
        ProductNormalizerImpl.normalizeAll(listOf(product)).single()

    private fun product(
        id: Long = 1,
        price: Price = Price(BigDecimal("12.5"), Currency.USD),
        weight: Weight = Weight(BigDecimal("10"), WeightUnit.LB),
    ): Product = Product(
        id = id,
        name = "Mobil 1, Synthetic",
        type = ProductType.OIL,
        price = price,
        weight = weight,
        vehicle = VehicleType.PASSENGER,
        extra = mapOf("volume" to "4"),
    )
}