package org.example.validator

import org.example.exception.ProductValidationException
import org.example.model.Price
import org.example.model.Product
import org.example.model.RawProduct
import org.example.model.Weight
import org.example.report.RowError
import org.example.report.ValidationReport
import java.math.BigDecimal

object ProductValidatorImpl : ProductValidator {
    private val ALLOWED_COLORS = setOf("черный", "белый", "серебристый")

    override fun validateAll(raws: List<RawProduct>): ValidationReport {
        val valid = mutableListOf<Product>()
        val errors = mutableListOf<RowError>()

        raws.forEach { raw ->
            runCatching { validate(raw) }
                .onSuccess { valid.add(it) }
                .onFailure { e ->
                    errors.add(
                        RowError(
                            lineNumber = raw.lineNumber,
                            row = raw.toRow(),
                            reason = e.message ?: "Ошибка валидации",
                        )
                    )
                }
        }

        return ValidationReport(valid, errors)
    }

    private fun validate(raw: RawProduct): Product {
        requireName(raw.name)

        requirePositive(raw.priceAmount, "Цена")

        requirePositive(raw.weightAmount, "Вес")

        requireColorAllowed(raw.extra)

        return Product(
            id = raw.id,
            name = raw.name,
            type = raw.type,
            price = Price(raw.priceAmount, raw.priceCurrency),
            weight = Weight(raw.weightAmount, raw.weightUnit),
            vehicle = raw.vehicle,
            extra = raw.extra,
        )
    }

    private fun requireName(name: String) {
        if (name.isBlank()) {
            throw ProductValidationException("Пустое название товара")
        }
    }

    private fun requirePositive(amount: BigDecimal, what: String) {
        if (amount.signum() <= 0) {
            throw ProductValidationException("$what должна быть положительной, получено: $amount")
        }
    }

    private fun requireColorAllowed(extra: Map<String, String>) {
        val color = extra["color"] ?: return
        if (color !in ALLOWED_COLORS) {
            throw ProductValidationException(
                "Недопустимый цвет: '$color'. Разрешены: ${ALLOWED_COLORS.joinToString()}"
            )
        }
    }

    private fun RawProduct.toRow(): List<String> =
        listOf(
            id.toString(),
            name,
            type.name.lowercase(),
            "$priceAmount ${priceCurrency.name}",
            "$weightAmount ${weightUnit.name}",
            vehicle.name,
            extra.entries.joinToString(";") { "${it.key}=${it.value}" },
        )
}