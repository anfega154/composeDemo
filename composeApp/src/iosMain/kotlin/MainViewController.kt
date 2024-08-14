import Di.appModule
import androidx.compose.ui.window.ComposeUIViewController
import com.demo.Database
import data.DatabaseDriverFactory
import org.koin.core.context.startKoin
import Utils.Mantum

fun MainViewController() = ComposeUIViewController { App() }

fun initKoin() {
    val mantum = Mantum()
   startKoin{
         modules(appModule(Database.invoke(DatabaseDriverFactory().createDriver()),mantum))
   }.koin
}