package id.stargan.intikasir.data.model

data class ActivationResponse(
    val success: Boolean,
    val message: String,
    val signature: String?,
    val expiry: Long? // timestamp
)

data class ActivationRequest(
    val serialNumber: String,
    val deviceId: String
)

