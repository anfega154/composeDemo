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
    var showForm by mutableStateOf(false)
    var equipos by mutableStateOf(listOf<Equipo>())

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

    fun addEquipo(equipo: Equipo) {
        viewModelScope.launch {
            equipoRepository.addEquipo(equipo)
            getAllEquipos()
            showForm = false
        }
    }

    fun toggleShowForm() {
        showForm = !showForm
    }

    fun deleteAllEquipos() {
        equipoRepository.deleteAllEquipos()
    }

}