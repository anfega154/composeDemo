package Model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val success: Boolean? = null,
    val message: String? = null,
    val body: T
)

