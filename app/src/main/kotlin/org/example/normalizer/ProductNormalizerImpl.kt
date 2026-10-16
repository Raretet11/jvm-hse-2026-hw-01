package org.example.normalizer

import org.example.model.Currency
import org.example.model.Price
import org.example.model.Product
import org.example.model.Weight
import org.example.model.WeightUnit
import java.math.BigDecimal
import java.math.RoundingMode

object ProductNormalizerImpl : ProductNormalizer {
    private val USD_TO_RUB_COURSE = BigDecimal(90)
    private val LB_TO_KG = BigDecimal(0.454)
    private const val BIG_DECIMAL_SCALE = 5

    override fun normalizeAll(products: List<Product>): List<Product> =
        products.map { normalize(it) }

    private fun normalize(product: Product): Product =
        product.copy(
            price = normalizePrice(product.price),
            weight = normalizeWeight(product.weight),
        )

    private fun normalizePrice(price: Price): Price {
        if (price.currency == Currency.USD) {
            return Price(
                price.amount
                    .multiply(USD_TO_RUB_COURSE)
                    .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP),
                Currency.RUB
            )
        }
        return price
    }

    private fun normalizeWeight(weight: Weight): Weight {
        if (weight.unit == WeightUnit.LB) {
            return Weight(
                weight.amount
                    .multiply(LB_TO_KG)
                    .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP),
                WeightUnit.KG
            )
        }
        return weight
    }
}