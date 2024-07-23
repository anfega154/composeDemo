package Domain

import Model.Equipo
import app.cash.sqldelight.db.SqlDriver
import com.demo.Database
import com.demo.EquipoQueries


class EquipoRepository(database: Database) {
    private val equipoQueries = database.equipoQueries

    fun addEquipo(equipo: Equipo) {
        equipoQueries.insertEquipo(
            codigo = equipo.codigo,
            nombre = equipo.nombre,
            instalacionDeProceso = equipo.instalacionDeProceso,
            tamano = equipo.tamano,
            estado = if (equipo.estado) 1L else 0L,
            observaciones = equipo.observaciones)
    }

    fun getAllEquipos(): List<Equipo> {
        return equipoQueries.selectAllEquipos().executeAsList().map {
            Equipo(
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