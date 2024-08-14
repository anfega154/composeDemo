package Domain

import Model.Equipo
import Model.networkEquipo
import Utils.Request
import kotlinx.serialization.builtins.ListSerializer

private const val TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkdyZWciLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

class EquipoRepository(
    baseRepository: BaseRepository
) : BaseRepository(
    baseRepository.getDatabase(),
    baseRepository.getMantum(),
    baseRepository.getHttpClient()
), EquipoInterface {
    private val equipoQueries = baseRepository.getDatabase().equipoQueries
    private val request = Request(baseRepository.getHttpClient())
    private val mantum = baseRepository.getMantum()

    override suspend fun addEquipo(equipo: Equipo) {
        try {
            request.post(
                url = "/v1/equipo",
                headers = mapOf("Authorization" to TOKEN),
                body = networkEquipo(
                    codigo = equipo.codigo,
                    nombre = equipo.nombre,
                    instalacionDeProceso = equipo.instalacionDeProceso,
                    tamano = equipo.tamano,
                    estado = if (equipo.estado) "activo" else "inactivo",
                    observaciones = equipo.observaciones
                ),
                serializer = networkEquipo.serializer()
            )
            equipoQueries.insertEquipo(
                codigo = equipo.codigo,
                nombre = equipo.nombre,
                instalacionDeProceso = equipo.instalacionDeProceso,
                tamano = equipo.tamano,
                estado = if (equipo.estado) 1L else 0L,
                observaciones = equipo.observaciones
            )
        } catch (e: Exception) {
            println("Error fetching equipos: ${e.message}")
        }

    }

    override suspend fun getAllEquipos(): List<Equipo> {
        return if (mantum.isConnectedOrConnecting()) {
            try {
                val networkResponse = request.get(
                    url = "/v1/equipo",
                    headers = mapOf("Authorization" to TOKEN),
                    serializer = ListSerializer(networkEquipo.serializer())
                )
                if (networkResponse.success == false || networkResponse.body.isEmpty()) {
                    return emptyList()
                }
                val equipos = networkResponse.body.map { networkEquipo ->
                    Equipo(
                        id = networkEquipo.id,
                        codigo = networkEquipo.codigo,
                        nombre = networkEquipo.nombre,
                        instalacionDeProceso = networkEquipo.instalacionDeProceso,
                        tamano = networkEquipo.tamano,
                        estado = if (networkEquipo.estado == "activo") true else false,
                        observaciones = networkEquipo.observaciones
                    )
                }
                equipos.forEach {
                    equipoQueries.insertEquipo(
                        codigo = it.codigo,
                        nombre = it.nombre,
                        instalacionDeProceso = it.instalacionDeProceso,
                        tamano = it.tamano,
                        estado = if (it.estado) 1L else 0L,
                        observaciones = it.observaciones
                    )
                }
                equipos
            } catch (e: Exception) {
                println("Error fetching equipos: ${e.message}")
                emptyList()
            }
        } else {
            equipoQueries.selectAllEquipos().executeAsList().map {
                Equipo(
                    id = it.id,
                    codigo = it.codigo,
                    nombre = it.nombre,
                    instalacionDeProceso = it.instalacionDeProceso,
                    tamano = it.tamano,
                    estado = it.estado == 1L,
                    observaciones = it.observaciones
                )
            }
        }
    }

    override fun deleteAllEquipos() {
        equipoQueries.deleteAllEquipos()
    }
}