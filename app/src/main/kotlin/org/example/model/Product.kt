package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long,
    val name: String,
    val type: ProductType,
    val price: Price,
    val weight: Weight,
    val vehicle: VehicleType,
    val extra: Map<String, String>
)
