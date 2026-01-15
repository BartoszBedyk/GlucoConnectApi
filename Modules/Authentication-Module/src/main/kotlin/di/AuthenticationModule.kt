package di

import data.AuthenticationRepository
import domain.AuthenticationService
import JwtHelper
import org.koin.dsl.module

val authenticationModule = module {
    single { AuthenticationRepository() }
    single { AuthenticationService(get()) }
    single { JwtHelper() }
}
