package Presentation

import Domain.EquipoRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import Model.Equipo
import androidx.compose.runtime.rememberCoroutineScope
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlinx.coroutines.launch


class EquipoViewModel(private val equipoRepository: EquipoRepository) : ViewModel() {
    var equipos by mutableStateOf(listOf<Equipo>())

    sealed class AddEquipoResult {
        object Success : AddEquipoResult()
        object NoConnection : AddEquipoResult()
        data class Error(val exception: Exception) : AddEquipoResult()
    }

    init {
        viewModelScope.launch {
            getAllEquipos()
        }
    }

    fun getAllEquipos() {
        viewModelScope.launch {
            equipos = equipoRepository.getAllEquipos()
        }
    }

    suspend fun addEquipo(equipo: Equipo): AddEquipoResult {
        return try {
            val add = equipoRepository.addEquipo(equipo)
            getAllEquipos()
            if (add) {
                AddEquipoResult.Success
            } else {
                AddEquipoResult.NoConnection
            }
        } catch (e: Exception) {
            AddEquipoResult.Error(e)
        }
    }

    fun deleteAllEquipos() {
        equipoRepository.deleteAllEquipos()
    }

}