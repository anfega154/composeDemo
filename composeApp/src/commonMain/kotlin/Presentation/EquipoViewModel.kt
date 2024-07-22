package Presentation

import com.rickclephas.kmp.observableviewmodel.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import Model.Equipo
import com.rickclephas.kmp.observableviewmodel.launch

class EquipoViewModel : ViewModel() {
    var showForm by mutableStateOf(false)
    var equipos by mutableStateOf(listOf<Equipo>())
        private set

    fun addEquipo(equipo: Equipo) {
        equipos = equipos + equipo
        showForm = false
    }

    fun toggleShowForm() {
        showForm = !showForm
    }

}