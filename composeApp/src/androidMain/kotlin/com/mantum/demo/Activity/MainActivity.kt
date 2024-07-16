package com.mantum.demo.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mantum.demo.Entity.Equipo
import com.mantum.demo.ViewModel.EquipoViewModel
import ui.Field.BasicTextField.CustomTextField
import ui.Select.BasicSelect.DropdownSelector
import ui.Field.TextAreaField.LabeledTextField


class MainActivity : ComponentActivity() {
    private val equipoViewModel: EquipoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalApp(equipoViewModel)
        }
    }
}

@Composable
fun LocalApp(viewModel: EquipoViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { viewModel.toggleShowForm() },
            shape = MaterialTheme.shapes.extraSmall,) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        CustomTextField(
            value = codigo,
            onValueChange = { codigo = it },
            placeholder = "Código"
        )
        CustomTextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = "Nombre"
        )
        CustomTextField(
            value = instalacionDeProceso,
            onValueChange = { instalacionDeProceso = it },
            placeholder = "Instalación de Proceso"
        )

        CustomTextField(
            value = tamano,
            onValueChange = { tamano = it },
            placeholder = "Tamaño"
        )
        DropdownSelector(
            options = estadoOptions,
            selectedOption = if (estado) "Activo" else "Inactivo",
            onOptionSelected = { option ->
                estado = option == "Activo"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        LabeledTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = "Observaciones"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onSave(Equipo(codigo, nombre, instalacionDeProceso, tamano, estado, observaciones))
        }, shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Crear")
        }
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
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Código: ${equipo.codigo}", fontSize = 18.sp)
                    Text(text = "Nombre: ${equipo.nombre}", fontSize = 18.sp)
                    Text(
                        text = "Instalación de Proceso: ${equipo.instalacionDeProceso}",
                        fontSize = 18.sp
                    )
                    Text(text = "Tamaño: ${equipo.tamano}", fontSize = 18.sp)
                    Text(text = "Estado: ${if (equipo.estado) "Activo" else "Inactivo"}", fontSize = 18.sp)
                    Text(text = "Observaciones: ${equipo.observaciones}", fontSize = 18.sp)
                }
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    LocalApp(viewModel = EquipoViewModel())
}