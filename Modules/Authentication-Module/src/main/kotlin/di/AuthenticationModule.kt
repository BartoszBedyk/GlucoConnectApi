package di

import JwtHelper
import data.AuthenticationRepository
import domain.AuthenticationService
import org.koin.dsl.module

val authenticationModule = module {
    single { AuthenticationRepository() }
    single { AuthenticationService(get()) }
    single { JwtHelper() }
}
