package Navigation

import Presentation.EquipoViewModel
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import org.koin.core.parameter.parametersOf
import ui.EquipoScreen


@Composable
fun Navigation(navigator: Navigator) {

    val viewModel = koinViewModel(EquipoViewModel::class) { parametersOf() }

    NavHost(
        navigator = navigator,
        initialRoute = "/home"
    ) {
        scene(route = "/home") {
            EquipoScreen(viewModel)
        }
    }

}