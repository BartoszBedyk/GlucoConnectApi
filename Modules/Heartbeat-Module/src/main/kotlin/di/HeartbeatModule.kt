package di

import data.HeartbeatRepository
import domain.HeartbeatService
import org.koin.dsl.module

val heartbeatModule = module {
    single { HeartbeatRepository() }
    single { HeartbeatService(get()) }
}
