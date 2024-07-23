package Di

import Domain.EquipoRepository
import Presentation.EquipoViewModel
import com.demo.Database
import data.DatabaseDriverFactory
//import data.createDatabase
import org.koin.dsl.module


fun appModule(database: Database) = module {
    single { EquipoRepository(database) }
    factory { EquipoViewModel(get()) }
}