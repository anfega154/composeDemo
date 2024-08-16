package Domain

import Model.Equipo

interface EquipoInterface {

    suspend fun addEquipo(equipo: Equipo): Boolean
    suspend fun getAllEquipos(): List<Equipo>
    fun deleteAllEquipos()
}