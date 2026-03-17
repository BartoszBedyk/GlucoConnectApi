package di

import data.ActivityRepository
import domain.ActivityService
import org.koin.dsl.module

val activityModule = module {
    single { ActivityRepository() }
    single { ActivityService(get()) }
}
