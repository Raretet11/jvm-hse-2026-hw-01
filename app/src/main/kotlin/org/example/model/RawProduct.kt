package org.example.model

import java.math.BigDecimal

data class RawProduct(
    val lineNumber: Int,
    val id: Long,
    val name: String,
    val type: ProductType,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightAmount: BigDecimal,
    val weightUnit: WeightUnit,
    val vehicle: VehicleType,
    val extra: Map<String, String>,
)
