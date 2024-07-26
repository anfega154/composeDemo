import Navigation.Navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext


@Composable
@Preview
fun App() {
    PreComposeApp {
        val colors = getColorTheme()

        KoinContext {
            val navigator = rememberNavigator()
            val titleTopBar = getTitleTopBar(navigator)
            val isAddEquipo = titleTopBar != "Dashboard"
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(elevation = 0.dp,
                            title = {
                                Text(
                                    text = titleTopBar,
                                    fontSize = 25.sp,
                                    color = colors.textColor
                                )
                            },
                            navigationIcon = {
                                if (isAddEquipo) {
                                    IconButton(onClick = {
                                        navigator.popBackStack()
                                    }) {
                                        Icon(
                                            modifier = Modifier.padding(start = 16.dp),
                                            imageVector =
                                            Icons.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = colors.textColor
                                        )
                                    }
                                } else {
                                    Icon(
                                        modifier = Modifier.padding(start = 16.dp),
                                        imageVector =
                                        Icons.Filled.Home,
                                        contentDescription = "Dashboard",
                                        tint = colors.textColor
                                    )
                                }

                            }, backgroundColor = colors.backgroundColor
                        )


                    },floatingActionButton = {
                        if (!isAddEquipo) {
                            FloatingActionButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = {
                                    navigator.navigate("/addEquipo")
                                },
                                shape = RoundedCornerShape(50),
                                backgroundColor = colors.addIconColor,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    tint = Color.White,
                                    contentDescription = "Floatin Icon"
                                )
                            }
                        }
                    }
                ) {
                    Navigation(navigator)
                }
            }
        }
    }
}

@Composable
fun getTitleTopBar(navigator: Navigator): String {
    var titleTopBar = "Dashboard"
    if (navigator.currentEntry.collectAsState(null).value?.route?.route.equals("/addEquipo")) {
        titleTopBar = "Add Equipo"
    }
    return titleTopBar
}






