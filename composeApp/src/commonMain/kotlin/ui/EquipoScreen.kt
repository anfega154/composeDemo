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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getColorTheme
import ui.Button.BasicButton.GenericButton

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
    val jsonSchema = BasicKomino.basicKomino()
    val formSchema = parseJsonSchema(jsonSchema)
    var fieldValues = mapOf<String, String>()

    KominoForm(schema = formSchema, onFieldValuesChange = { values ->
        fieldValues = values
    })

    GenericButton(
        onClick = {
            val equipo = mapFieldValuesToEquipo(fieldValues)
            onSave(equipo)
        },
        text = "Guardar",
        modifier = Modifier.fillMaxWidth()
    )
}

fun mapFieldValuesToEquipo(fieldValues: Map<String, String>): Equipo {
    return Equipo(
        codigo = fieldValues["Código"] ?: "",
        nombre = fieldValues["Nombre"] ?: "",
        instalacionDeProceso = fieldValues["Instalación de Proceso"] ?: "",
        tamano = fieldValues["Tamaño"] ?: "",
        estado = fieldValues["Estado"] == "Activo",
        observaciones = fieldValues["Observaciones"] ?: ""
    )
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

