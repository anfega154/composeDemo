package Di

import Domain.EquipoRepository
import Presentation.EquipoViewModel
import com.demo.Database
import data.DatabaseDriverFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
//import data.createDatabase
import org.koin.dsl.module


fun appModule(database: Database) = module {
    single<HttpClient> { HttpClient{install(ContentNegotiation) {json()} } }
    single { EquipoRepository(database, get()) }
    factory { EquipoViewModel(get()) }
}