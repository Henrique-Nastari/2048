package com.hcn2048.di

import com.hcn2048.engine.GameEngine
import com.hcn2048.ui.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Platform-agnostic dependencies. Platform services (preferences, ads, billing, review)
 * are registered by each platform module (see `androidPlatformModule` on Android).
 */
val sharedModule = module {
    single { GameEngine() }

    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    single {
        GameViewModel(
            engine = get(),
            preferences = get(),
            ads = get(),
            billing = get(),
            reviewPrompt = get(),
            scope = get(),
        )
    }
}
