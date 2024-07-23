import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.material.MaterialTheme
import Presentation.EquipoViewModel
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.KoinContext
import org.koin.core.parameter.parametersOf
import ui.EquipoScreen


@Composable
@Preview
fun App() {
    PreComposeApp {
        KoinContext {
            val viewModel = koinViewModel(EquipoViewModel::class) { parametersOf() }
            //val viewModel = EquipoViewModel()
            AppTheme {
                EquipoScreen(viewModel)
            }
        }
    }
}






