import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import Presentation.EquipoViewModel
import ui.EquipoScreen



@Composable
@Preview
fun App() {
    val equipoViewModel = remember { EquipoViewModel() }
    MaterialTheme {
        EquipoScreen(equipoViewModel)
    }
}





