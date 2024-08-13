package Domain

import Model.Equipo
import Model.networkEquipo
import app.cash.sqldelight.db.SqlDriver
import com.demo.Database
import com.demo.EquipoQueries
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.headers

private const val BASE_URL = "http://10.0.2.2:8001/api"
private const val TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkdyZWciLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

class EquipoRepository(
    private val database: Database,
    private val httpClient: HttpClient
) : EquipoInterface {
    private val equipoQueries = database.equipoQueries

    override suspend fun addEquipo(equipo: Equipo) {
        equipoQueries.insertEquipo(
            codigo = equipo.codigo,
            nombre = equipo.nombre,
            instalacionDeProceso = equipo.instalacionDeProceso,
            tamano = equipo.tamano,
            estado = if (equipo.estado) 1L else 0L,
            observaciones = equipo.observaciones
        )
    }

    override suspend fun getAllEquipos(): List<Equipo> {
        return if (equipoQueries.selectAllEquipos().executeAsList().isEmpty()) {
            try {
                val networkResponse = httpClient.get("$BASE_URL/v1/equipo") {
                    headers {
                        append("Authorization", TOKEN)
                    }
                }.body<List<networkEquipo>>()
                if (networkResponse.isEmpty()) return emptyList()
                val equipos = networkResponse.map { networkEquipo ->
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