package id.stargan.intikasir.data.model

/**
 * New encrypted format response
 */
data class ActivationResponse(
    val ok: Boolean,
    val payload: String?, // Base64 encoded JSON
    val signature: String?, // Base64 signature of payload
    val error: String? = null,
    val message: String? = null
)

/**
 * Decrypted payload content
 */
data class ActivationPayload(
    val sn: String,
    val device_uuid: String,
    val expiry: Long = 0L, // Optional, default to 0 (no expiry)
    val tier: String = "basic",
    val activated_at: String? = null // Optional timestamp from some servers
)

/**
 * Request with encrypted cipher
 */
data class ActivationRequest(
    val cipher: String // Base64 RSA-OAEP encrypted JSON
)

/**
 * Plain request payload (before encryption)
 */
data class ActivationRequestPayload(
    val sn: String,
    val device_uuid: String
)

