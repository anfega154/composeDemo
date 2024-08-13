package Model

import kotlinx.serialization.Serializable

data class Equipo(
    val id: Long = -1,
    val codigo: String,
    val nombre: String,
    val instalacionDeProceso: String,
    val tamano: String,
    val estado: Boolean,
    val observaciones: String
)

@Serializable
data class networkEquipo(
    val id: Long = -1,
    val codigo: String,
    val nombre: String,
    val estado: String,
    val instalacionDeProceso: String,
    val tamano: String,
    val observaciones: String
)