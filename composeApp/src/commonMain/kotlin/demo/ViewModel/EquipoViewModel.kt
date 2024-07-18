package demo.ViewModel

import com.rickclephas.kmp.observableviewmodel.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import demo.Entity.Equipo

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