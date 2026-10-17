package com.hcn2048.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Tracks the currently resumed activity so platform services (ads, billing, review)
 * can present their UIs without leaking the activity.
 */
object ActivityProvider {

    private var currentRef: WeakReference<Activity>? = null

    val current: Activity?
        get() = currentRef?.get()

    fun register(context: Context) {
        val application = context.applicationContext as? Application ?: return
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    currentRef = WeakReference(activity)
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                override fun onActivityDestroyed(activity: Activity) {
                    if (currentRef?.get() === activity) {
                        currentRef = null
                    }
                }
            },
        )
    }
}
