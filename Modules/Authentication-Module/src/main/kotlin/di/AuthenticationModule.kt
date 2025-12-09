package di

import data.AuthenticationRepository
import domain.AuthenticationService
import domain.JwtHelper
import org.koin.dsl.module

val authenticationModule = module {
    single { AuthenticationRepository() }
    single { AuthenticationService(get()) }
    single { JwtHelper() }
}
