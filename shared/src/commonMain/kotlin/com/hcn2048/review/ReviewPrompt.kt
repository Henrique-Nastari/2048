package com.hcn2048.review

/**
 * Asks the platform for an in-app review.
 * Call it only on a positive moment (e.g. first time the player reaches 2048).
 */
interface ReviewPrompt {
    fun requestReview()
}
