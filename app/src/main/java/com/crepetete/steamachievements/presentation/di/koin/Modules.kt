package com.crepetete.steamachievements.presentation.di.koin

import com.crepetete.data.network.SteamApiService
import com.crepetete.steamachievements.data.database.SteamDatabase
import com.crepetete.steamachievements.data.repository.GameRepositoryImpl
import com.crepetete.steamachievements.data.repository.PreferencesRepositoryImpl
import com.crepetete.steamachievements.data.repository.UserRepositoryImpl
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import com.crepetete.steamachievements.domain.repository.UserRepository
import com.crepetete.steamachievements.domain.usecases.player.GetCurrentPlayerIdUseCase
import com.crepetete.steamachievements.domain.usecases.player.GetCurrentPlayerIdUseCaseImpl
import com.crepetete.steamachievements.domain.usecases.player.GetPlayerUseCase
import com.crepetete.steamachievements.domain.usecases.player.GetPlayerUseCaseImpl
import com.crepetete.steamachievements.domain.usecases.player.SaveCurrentPlayerIdUserCase
import com.crepetete.steamachievements.domain.usecases.player.SaveCurrentPlayerIdUserCaseImpl
import com.crepetete.steamachievements.presentation.activity.login.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Koin modules
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 17:56.
 */

val dataModules = module(override = true) {
    // API
    single { SteamApiService.buildApiService(androidContext()) }

    // Room Database
    single { SteamDatabase.buildDatabase(androidContext()) }

    // Repositories
    single<GameRepository> {
        GameRepositoryImpl(
            get<SteamDatabase>().achievementsDao(),
            get(),
            get<SteamDatabase>().gamesDao(),
            get<SteamDatabase>().newsDao()
        )
    }

    single<UserRepository> { UserRepositoryImpl(get(), get(), get<SteamDatabase>().playerDao()) }

    single<PreferencesRepository> { PreferencesRepositoryImpl(androidContext()) }
}

val domainModules = module(override = true) {
    single { createGetCurrentPlayerIdUseCase(get()) }
    single { createGetPlayerUseCase(get()) }
    single { createSaveCurrentPlayerIdUserCase(get()) }
}

val presentationModules = module(override = true) {
    // ViewModels
    viewModel { AuthViewModel(get(), get(), get()) }
}

// UseCases
fun createGetCurrentPlayerIdUseCase(userRepo: UserRepository): GetCurrentPlayerIdUseCase {
    return GetCurrentPlayerIdUseCaseImpl(userRepo)
}

fun createGetPlayerUseCase(userRepo: UserRepository): GetPlayerUseCase {
    return GetPlayerUseCaseImpl(userRepo)
}

fun createSaveCurrentPlayerIdUserCase(prefsRepo: PreferencesRepository): SaveCurrentPlayerIdUserCase {
    return SaveCurrentPlayerIdUserCaseImpl(prefsRepo)
}