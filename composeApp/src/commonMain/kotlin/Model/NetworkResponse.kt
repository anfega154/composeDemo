package Model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val success: Boolean,
    val message: String,
    val body: T
)