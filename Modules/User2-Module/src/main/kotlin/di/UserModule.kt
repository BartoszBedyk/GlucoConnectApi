package di

import data.UserRepository
import domain.UserService
import org.koin.dsl.module


val userModule = module {
    single { UserRepository() }
    single { UserService(get()) }

}
