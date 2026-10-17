package com.hcn2048.data

import kotlinx.coroutines.flow.Flow

/**
 * Persistent player preferences and local game data.
 * Single source of truth backed by DataStore on Android.
 */
interface PreferencesRepository {
    val themeId: Flow<String>
    val hapticsEnabled: Flow<Boolean>
    val adsRemoved: Flow<Boolean>
    val bestScore: Flow<Int>
    val savedGame: Flow<String?>
    val gameOverCount: Flow<Int>
    val reviewRequested: Flow<Boolean>
    val freeUndoUsed: Flow<Boolean>

    suspend fun setThemeId(value: String)
    suspend fun setHapticsEnabled(value: Boolean)
    suspend fun setAdsRemoved(value: Boolean)
    suspend fun setBestScore(value: Int)
    suspend fun saveGame(value: String?)
    suspend fun setGameOverCount(value: Int)
    suspend fun setReviewRequested(value: Boolean)
    suspend fun setFreeUndoUsed(value: Boolean)
}
