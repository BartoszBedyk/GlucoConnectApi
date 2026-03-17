package di

import data.DrugRepository
import domain.DrugService
import org.koin.dsl.module

val drugModule =
    module {
        single { DrugRepository() }
        single { DrugService(get()) }
    }
