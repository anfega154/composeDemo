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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import getColorTheme
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import ui.Button.BasicButton.GenericButton


@Composable
fun EquipoScreen(viewModel: EquipoViewModel, navigator: Navigator) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(16.dp)) {
        EquipoForm(onSave = { equipo ->
            coroutineScope.launch {
                viewModel.addEquipo(equipo)
            }
            navigator.navigate("/home")
        })

        Spacer(modifier = Modifier.height(16.dp))

        GenericButton(
            text = "Ver Equipos",
            onClick = { navigator.navigate("/home") },
            modifier = Modifier.fillMaxWidth(),
            color = "mantum"
        )
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
        modifier = Modifier.fillMaxWidth(),
        color = "Success"
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


