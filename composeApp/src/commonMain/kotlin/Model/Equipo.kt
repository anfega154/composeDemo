package Model

data class Equipo(
    val codigo: String,
    val nombre: String,
    val instalacionDeProceso: String,
    val tamano: String,
    val estado: Boolean,
    val observaciones: String
)