package com.hcn2048.di

import com.hcn2048.ads.AdIds
import com.hcn2048.ads.AdManager
import com.hcn2048.ads.AdManagerImpl
import com.hcn2048.billing.BillingManager
import com.hcn2048.billing.BillingManagerImpl
import com.hcn2048.data.PreferencesRepository
import com.hcn2048.data.PreferencesRepositoryImpl
import com.hcn2048.review.ReviewPrompt
import com.hcn2048.review.ReviewPromptImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android platform services. The app supplies its AdMob IDs (production values or test ones).
 * `Context` comes from Koin's `androidContext`.
 */
fun androidPlatformModule(adIds: AdIds): Module = module {
    single { adIds }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single<AdManager> { AdManagerImpl(get(), get()) }
    single<BillingManager> { BillingManagerImpl(get()) }
    single<ReviewPrompt> { ReviewPromptImpl(get()) }
}
