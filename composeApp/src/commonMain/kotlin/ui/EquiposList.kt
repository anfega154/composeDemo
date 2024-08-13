package ui

import Presentation.EquipoViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EquiposList(viewModel: EquipoViewModel) {
    val equipos = viewModel.equipos
    //viewModel.deleteAllEquipos()
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