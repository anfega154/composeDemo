package Presentation

import Domain.EquipoRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import Model.Equipo
import com.demo.Database
import moe.tlaster.precompose.viewmodel.viewModelScope
import data.DatabaseDriverFactory
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
        equipos = equipoRepository.getAllEquipos()
    }

    fun addEquipo(equipo: Equipo) {
      equipoRepository.addEquipo(equipo)
      equipos = equipoRepository.getAllEquipos()
        //equipos += equipo
        showForm = false
    }

    fun toggleShowForm() {
        showForm = !showForm
    }

}