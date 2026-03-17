package di

import data.GlucoseRepository
import domain.GlucoseService
import org.koin.dsl.module

val glucoseModule = module {
    single { GlucoseRepository() }
    single { GlucoseService(get()) }
}
