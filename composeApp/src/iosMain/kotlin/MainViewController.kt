import Di.appModule
import androidx.compose.ui.window.ComposeUIViewController
import com.demo.Database
import data.DatabaseDriverFactory
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { App() }

fun initKoin() {
   startKoin{
         modules(appModule(Database.invoke(DatabaseDriverFactory().createDriver())))
   }.koin
}