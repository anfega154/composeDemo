package Di

import Domain.BaseRepository
import Domain.EquipoRepository
import Presentation.EquipoViewModel
import Utils.Mantum
import com.demo.Database
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module


fun appModule(database: Database, mantum: Mantum) = module {
    single<HttpClient> { HttpClient{install(ContentNegotiation) {json()} } }
    single { mantum }
    single { BaseRepository(database, get(),get()) }
    factory { EquipoRepository(get())}
    factory { EquipoViewModel(get()) }
}