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
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import ui.Button.BasicButton.GenericButton
import androidx.compose.material.Text
import getColorTheme

@Composable
fun EquipoScreen(viewModel: EquipoViewModel, navigator: Navigator) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Column(modifier = Modifier.padding(16.dp)) {
        EquipoForm(onSave = { equipo ->
            coroutineScope.launch {
                val result = viewModel.addEquipo(equipo)
                when (result) {
                    is EquipoViewModel.AddEquipoResult.Success -> {
                        navigator.navigate("/home")
                    }
                    is EquipoViewModel.AddEquipoResult.NoConnection -> {
                        showSnackbar(snackbarHostState, "No tienes conexión a internet")
                    }
                    is EquipoViewModel.AddEquipoResult.Error -> {
                        showSnackbar(snackbarHostState, "Error al guardar el equipo: ${result.exception.message}")
                    }
                }
            }

        })

        Spacer(modifier = Modifier.height(16.dp))

        GenericButton(
            text = "Ver Equipos",
            onClick = { navigator.navigate("/home") },
            modifier = Modifier.fillMaxWidth(),
            color = "mantum"
        )
    }
    SnackbarHost(hostState = snackbarHostState)
}


@Composable
fun EquipoForm(onSave: (Equipo) -> Unit) {
    val jsonSchema = BasicKomino.basicKomino()
    val formSchema = parseJsonSchema(jsonSchema)
    val fieldValues = remember { mutableStateOf(mapOf<String, String>()) }
    val errorMessage = remember { mutableStateOf("") }
    val Color = getColorTheme()


    KominoForm(schema = formSchema, onFieldValuesChange = { values ->
        fieldValues.value = values
    })

    GenericButton(
        onClick = {
            val equipo = mapFieldValuesToEquipo(fieldValues.value)
            if (validateFields(fieldValues.value)) {
                onSave(equipo)
                errorMessage.value = ""
            } else {
                errorMessage.value = "Todos los campos deben estar diligenciados"
            }
        },
        text = "Guardar",
        modifier = Modifier.fillMaxWidth(),
        color = "Success"
    )

    if (errorMessage.value.isNotEmpty()) {
        Text(
            text = errorMessage.value,
            color = Color.danger,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
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
suspend fun showSnackbar(snackbarHostState: SnackbarHostState, message: String) {
    snackbarHostState.showSnackbar(message)
}

fun validateFields(fieldValues: Map<String, String>): Boolean {
    return fieldValues.isNotEmpty() && fieldValues.values.all { it.isNotBlank() }
}


