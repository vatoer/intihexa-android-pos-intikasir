package id.stargan.intikasir.feature.auth.util

/**
 * PIN validation utilities
 */
object PinValidator {

    private const val MIN_PIN_LENGTH = 4
    private const val MAX_PIN_LENGTH = 6

    /**
     * Validate PIN format
     *
     * @param pin PIN to validate
     * @return ValidationResult with success or error message
     */
    fun validate(pin: String): ValidationResult {
        return when {
            pin.isBlank() -> {
                ValidationResult.Error("PIN tidak boleh kosong")
            }
            !pin.all { it.isDigit() } -> {
                ValidationResult.Error("PIN harus berupa angka")
            }
            pin.length < MIN_PIN_LENGTH -> {
                ValidationResult.Error("PIN minimal $MIN_PIN_LENGTH digit")
            }
            pin.length > MAX_PIN_LENGTH -> {
                ValidationResult.Error("PIN maksimal $MAX_PIN_LENGTH digit")
            }
            else -> {
                ValidationResult.Success
            }
        }
    }

    /**
     * Check if PIN is strong enough (no simple patterns)
     *
     * @param pin PIN to check
     * @return ValidationResult with success or warning
     */
    fun checkStrength(pin: String): ValidationResult {
        if (pin.length < MIN_PIN_LENGTH) {
            return ValidationResult.Error("PIN terlalu pendek")
        }

        // Check for simple patterns
        val weakPatterns = listOf(
            "0000", "1111", "2222", "3333", "4444",
            "5555", "6666", "7777", "8888", "9999",
            "1234", "4321", "0123", "3210"
        )

        if (weakPatterns.any { pin.contains(it) }) {
            return ValidationResult.Warning("PIN terlalu mudah ditebak. Gunakan kombinasi yang lebih kuat.")
        }

        // Check for consecutive numbers
        if (pin.length >= 3) {
            for (i in 0 until pin.length - 2) {
                val num1 = pin[i].digitToIntOrNull() ?: continue
                val num2 = pin[i + 1].digitToIntOrNull() ?: continue
                val num3 = pin[i + 2].digitToIntOrNull() ?: continue

                if (num2 == num1 + 1 && num3 == num2 + 1) {
                    return ValidationResult.Warning("PIN memiliki angka berurutan. Pertimbangkan kombinasi yang lebih kuat.")
                }
            }
        }

        return ValidationResult.Success
    }

    /**
     * Validation result
     */
    sealed class ValidationResult {
        data object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
    }
}

