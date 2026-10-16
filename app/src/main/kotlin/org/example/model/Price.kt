package org.example.model

import kotlinx.serialization.Serializable
import org.example.utils.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Price(
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val currency: Currency
)
