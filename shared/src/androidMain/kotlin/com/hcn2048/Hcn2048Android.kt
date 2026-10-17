package com.hcn2048

import android.content.Context
import com.hcn2048.ads.AdIds
import com.hcn2048.ads.AdsConsent
import com.hcn2048.di.androidPlatformModule
import com.hcn2048.di.sharedModule
import com.hcn2048.util.ActivityProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/** One-call bootstrap for the Android app: Koin, ads consent and activity tracking. */
object Hcn2048Android {

    fun init(context: Context, adIds: AdIds) {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(context)
            modules(sharedModule, androidPlatformModule(adIds))
        }
        ActivityProvider.register(context)
        AdsConsent.initialize(context)
    }
}
