package com.hcn2048.game

import android.app.Application
import com.hcn2048.Hcn2048Android
import com.hcn2048.ads.AdIds

class Hcn2048Application : Application() {

    override fun onCreate() {
        super.onCreate()
        Hcn2048Android.init(
            context = this,
            adIds = AdIds(
                banner = BuildConfig.ADMOB_BANNER_ID,
                interstitial = BuildConfig.ADMOB_INTERSTITIAL_ID,
                rewarded = BuildConfig.ADMOB_REWARDED_ID,
            ),
        )
    }
}
