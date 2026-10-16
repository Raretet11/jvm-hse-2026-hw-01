package org.example.parser

import org.example.exception.ProductParseException
import org.example.model.Currency
import org.example.model.ProductType
import org.example.model.RawProduct
import org.example.model.Season
import org.example.model.VehicleType
import org.example.model.WeightUnit
import org.example.report.ParseReport
import org.example.report.RowError
import java.math.BigDecimal

object ProductParserImpl : ProductParser {
    private const val COLUMNS = 7

    private val PRICE_REGEX = Regex(
        """^(?:(\$|руб)(-?\d+(?:\.\d+)?)|(-?\d+(?:\.\d+)?)(\$|руб))$"""
    )

    private val WEIGHT_REGEX = Regex("""^(\d+(?:\.\d+)?)(kg|lb)$""")

    private val ALLOWED_EXTRA_KEYS: Map<ProductType, Set<String>> = mapOf(
        ProductType.OIL to setOf("volume"),
        ProductType.TIRE to setOf("diameter", "season"),
        ProductType.ACCESSORY to setOf("material", "color"),
    )

    private val SEASON_VALUES = setOf(Season.SUMMER.toString(), Season.WINTER.toString(), Season.ALL_SEASON.toString())

    private val NUMERIC_EXTRA_KEYS = setOf("volume", "diameter")

    override fun parseAll(rows: List<List<String>>): ParseReport {
        val parsed = mutableListOf<RawProduct>()
        val errors = mutableListOf<RowError>()

        rows.forEachIndexed { index, row ->
            if (row.isEmpty()) return@forEachIndexed

            runCatching { parseRow(row, index + 1) }
                .onSuccess { parsed.add(it) }
                .onFailure { e ->
                    errors.add(
                        RowError(
                            lineNumber = index + 1,
                            row = row,
                            reason = e.message ?: "Ошибка парсинга",
                        )
                    )
                }
        }

        return ParseReport(parsed, errors)
    }

    private fun parseRow(row: List<String>, lineNumber: Int): RawProduct {
        if (row.size != COLUMNS) {
            throw ProductParseException("Ожидалось $COLUMNS колонок, получено ${row.size}")
        }

        val rawId = row[0].trim()
        val rawName = row[1]
        val rawType = row[2].trim()
        val rawPrice = row[3].trim()
        val rawWeight = row[4].trim()
        val rawVehicle = row[5].trim()
        val rawExtra = row[6].trim()

        requireNotEmpty(rawId, "id")
        requireNotEmpty(rawType, "type")
        requireNotEmpty(rawPrice, "price")
        requireNotEmpty(rawWeight, "weight")
        requireNotEmpty(rawVehicle, "vehicle")

        val id = rawId.toLongOrNull()
            ?: throw ProductParseException("Не удалось распарсить id: '$rawId'")

        val type = parseType(rawType)
        val vehicle = parseVehicle(rawVehicle)
        val (priceAmount, priceCurrency) = parsePrice(rawPrice)
        val (weightAmount, weightUnit) = parseWeight(rawWeight)
        val extra = parseExtra(rawExtra, type)

        return RawProduct(
            lineNumber = lineNumber,
            id = id,
            name = rawName,
            type = type,
            priceAmount = priceAmount,
            priceCurrency = priceCurrency,
            weightAmount = weightAmount,
            weightUnit = weightUnit,
            vehicle = vehicle,
            extra = extra,
        )
    }

    private fun requireNotEmpty(value: String, field: String) {
        if (value.isBlank()) {
            throw ProductParseException("Пустое обязательное поле: '$field'")
        }
    }

    private fun parseType(raw: String): ProductType =
        when (raw.lowercase()) {
            "oil" -> ProductType.OIL
            "tire" -> ProductType.TIRE
            "accessory" -> ProductType.ACCESSORY
            else -> throw ProductParseException("Неизвестный тип товара: '$raw'")
        }

    private fun parseVehicle(raw: String): VehicleType =
        when (raw.uppercase()) {
            "PASSENGER" -> VehicleType.PASSENGER
            "TRUCK" -> VehicleType.TRUCK
            "MOTORCYCLE" -> VehicleType.MOTORCYCLE
            else -> throw ProductParseException("Неизвестный тип транспорта: '$raw'")
        }

    private fun parsePrice(raw: String): Pair<BigDecimal, Currency> {
        val match = PRICE_REGEX.matchEntire(raw)
            ?: throw ProductParseException("Не удалось распарсить цену: '$raw'")

        val currencyStr: String
        val amountStr: String
        if (match.groupValues[1].isNotEmpty()) {
            currencyStr = match.groupValues[1]
            amountStr = match.groupValues[2]
        } else {
            amountStr = match.groupValues[3]
            currencyStr = match.groupValues[4]
        }

        val amount = amountStr.toBigDecimalOrNull()
            ?: throw ProductParseException("Не удалось распарсить число цены: '$raw'")

        val currency = when (currencyStr) {
            "$" -> Currency.USD
            "руб" -> Currency.RUB
            else -> throw ProductParseException("Неизвестная валюта: '$raw'")
        }

        return amount to currency
    }

    private fun parseWeight(raw: String): Pair<BigDecimal, WeightUnit> {
        val match = WEIGHT_REGEX.matchEntire(raw)
            ?: throw ProductParseException("Не удалось распарсить вес: '$raw'")

        val amount = match.groupValues[1].toBigDecimalOrNull()
            ?: throw ProductParseException("Не удалось распарсить число веса: '$raw'")

        val unit = when (match.groupValues[2]) {
            "kg" -> WeightUnit.KG
            "lb" -> WeightUnit.LB
            else -> throw ProductParseException("Неизвестная единица веса: '$raw'")
        }

        return amount to unit
    }

    private fun parseExtra(raw: String, type: ProductType): Map<String, String> {
        if (raw.isBlank()) return emptyMap()

        val allowedKeys = ALLOWED_EXTRA_KEYS[type].orEmpty()
        val result = mutableMapOf<String, String>()

        raw.split(';').forEach { pair ->
            if (pair.isBlank()) return@forEach

            val parts = pair.split('=', limit = 2)
            if (parts.size != 2) {
                throw ProductParseException("Не удалось распарсить пару extra: '$pair'")
            }

            val key = parts[0].trim()
            val value = parts[1].trim()

            if (key !in allowedKeys) return@forEach

            if (key in NUMERIC_EXTRA_KEYS && value.toDoubleOrNull() == null) {
                throw ProductParseException("$key должно быть числом, получено '$value'")
            }

            if (key == "season" && value !in SEASON_VALUES) {
                throw ProductParseException("Недопустимое значение season=$value")
            }

            result[key] = value
        }

        return result
    }
}