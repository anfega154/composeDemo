package ui

import Komino.JsonToKomino.BasicKomino
import Komino.KominoForm
import Komino.Ultils.parseJsonSchema
import Model.Equipo
import Presentation.EquipoViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getColorTheme
import ui.Field.BasicTextField.CustomTextField
import ui.Field.TextAreaField.LabeledTextField
import ui.BasicSelect.DropdownSelector

@Composable
fun EquipoScreen(viewModel: EquipoViewModel) {
    val color = getColorTheme()
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { viewModel.toggleShowForm() },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = color.mantum)
        ) {
            Text(text = if (viewModel.showForm) "Ver Equipos" else "Crear Equipo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.showForm) {
            EquipoForm(onSave = { equipo ->
                viewModel.addEquipo(equipo)
            })
        } else {
            EquiposList(viewModel.equipos)
        }
    }
}


@Composable
fun EquipoForm(onSave: (Equipo) -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var instalacionDeProceso by remember { mutableStateOf("") }
    var tamano by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val estadoOptions = listOf("Activo", "Inactivo")
    var observaciones by remember { mutableStateOf("") }
    val color = getColorTheme()
    val jsonSchema = BasicKomino.basicKomino()
    val formSchema = parseJsonSchema(jsonSchema)

KominoForm(schema = formSchema) { formData ->
    onSave(
        Equipo(
            codigo = formData["Código"] ?: "",
            nombre = formData["Nombre"] ?: "",
            instalacionDeProceso = formData["Instalación de Proceso"] ?: "",
            tamano = formData["Tamaño"] ?: "",
            estado = formData["Estado"] == "Activo",
            observaciones = formData["Observaciones"] ?: ""
        )
    )
}

}

@Composable
fun EquiposList(equipos: List<Equipo>) {
    LazyColumn {
        items(equipos.size) { index ->
            val equipo = equipos[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(4.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Código: ${equipo.codigo}", fontSize = 18.sp)
                    Text(text = "Nombre: ${equipo.nombre}", fontSize = 18.sp)
                    Text(
                        text = "Instalación de Proceso: ${equipo.instalacionDeProceso}",
                        fontSize = 18.sp
                    )
                    Text(text = "Tamaño: ${equipo.tamano}", fontSize = 18.sp)
                    Text(
                        text = "Estado: ${if (equipo.estado) "Activo" else "Inactivo"}",
                        fontSize = 18.sp
                    )
                    Text(text = "Observaciones: ${equipo.observaciones}", fontSize = 18.sp)
                }
            }

        }

    }

}

