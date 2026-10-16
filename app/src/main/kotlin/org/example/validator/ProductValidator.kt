package org.example.validator

import org.example.model.RawProduct
import org.example.report.ValidationReport

interface ProductValidator {
    fun validateAll(raws: List<RawProduct>): ValidationReport
}
