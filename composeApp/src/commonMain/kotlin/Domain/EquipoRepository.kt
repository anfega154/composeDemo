package Domain

import Model.Equipo
import Model.networkEquipo
import Utils.Request
import com.demo.Database
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val BASE_URL = "http://10.0.2.2:8002/api"
//private const val BASE_URL = "http://192.168.0.78/laraveldemo/public/api"
//private const val BASE_URL = "http://127.0.0.1:8002/api"

private const val TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkdyZWciLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

class EquipoRepository(
    private val database: Database,
    private val httpClient: HttpClient
) : EquipoInterface {
    private val equipoQueries = database.equipoQueries
    private val request = Request(httpClient)

    override suspend fun addEquipo(equipo: Equipo) {
        try {
             val response = httpClient.post("$BASE_URL/v1/equipo") {
                headers {
                    append("Authorization", TOKEN)
                }
                contentType(ContentType.Application.Json)
                setBody(
                    networkEquipo(
                        codigo = equipo.codigo,
                        nombre = equipo.nombre,
                        instalacionDeProceso = equipo.instalacionDeProceso,
                        tamano = equipo.tamano,
                        estado = if (equipo.estado) "activo" else "inactivo",
                        observaciones = equipo.observaciones

                    )
                )
            }
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
        return if (equipoQueries.selectAllEquipos().executeAsList().isEmpty()) {
            try {
                val networkResponse = request.get<List<networkEquipo>>("/v1/equipo", mapOf("Authorization" to TOKEN))
                if (!networkResponse.success || networkResponse.body.isEmpty()) {
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