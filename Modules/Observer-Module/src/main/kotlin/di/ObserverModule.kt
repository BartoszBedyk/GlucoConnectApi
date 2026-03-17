package di

import JwtHelper
import data.ObserverRepository
import domain.ObserverService
import org.koin.dsl.module

val observerModule = module {
    single { ObserverRepository() }
    single { ObserverService(get()) }
    single { JwtHelper() }
}
