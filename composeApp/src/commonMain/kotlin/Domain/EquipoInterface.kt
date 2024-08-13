package Domain

import Model.Equipo

interface EquipoInterface {

    suspend fun addEquipo(equipo: Equipo)
    suspend fun getAllEquipos(): List<Equipo>
    fun deleteAllEquipos()
}