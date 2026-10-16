package org.example.normalizer

import org.example.model.Product

interface ProductNormalizer {
    fun normalizeAll(products: List<Product>): List<Product>
}
