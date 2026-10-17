package com.hcn2048.review

import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.hcn2048.util.ActivityProvider

/** Play In-App Review implementation (the system decides whether the dialog is shown). */
class ReviewPromptImpl(private val context: Context) : ReviewPrompt {

    override fun requestReview() {
        val activity = ActivityProvider.current ?: return
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }
}
