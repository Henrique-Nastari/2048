package com.hcn2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hcn2048DataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hcn2048_preferences",
)

/** DataStore-backed implementation of [PreferencesRepository]. */
class PreferencesRepositoryImpl(private val context: Context) : PreferencesRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val BEST_SCORE = intPreferencesKey("best_score")
        val SAVED_GAME = stringPreferencesKey("saved_game")
        val GAME_OVER_COUNT = intPreferencesKey("game_over_count")
        val REVIEW_REQUESTED = booleanPreferencesKey("review_requested")
        val FREE_UNDO_USED = booleanPreferencesKey("free_undo_used")
    }

    override val themeId: Flow<String> =
        context.hcn2048DataStore.data.map { it[Keys.THEME] ?: "CLASSIC" }

    override val hapticsEnabled: Flow<Boolean> =
        context.hcn2048DataStore.data.map { it[Keys.HAPTICS] ?: true }

    override val adsRemoved: Flow<Boolean> =
        context.hcn2048DataStore.data.map { it[Keys.ADS_REMOVED] ?: false }

    override val bestScore: Flow<Int> =
        context.hcn2048DataStore.data.map { it[Keys.BEST_SCORE] ?: 0 }

    override val savedGame: Flow<String?> =
        context.hcn2048DataStore.data.map { it[Keys.SAVED_GAME] }

    override val gameOverCount: Flow<Int> =
        context.hcn2048DataStore.data.map { it[Keys.GAME_OVER_COUNT] ?: 0 }

    override val reviewRequested: Flow<Boolean> =
        context.hcn2048DataStore.data.map { it[Keys.REVIEW_REQUESTED] ?: false }

    override val freeUndoUsed: Flow<Boolean> =
        context.hcn2048DataStore.data.map { it[Keys.FREE_UNDO_USED] ?: false }

    override suspend fun setThemeId(value: String) {
        context.hcn2048DataStore.edit { it[Keys.THEME] = value }
    }

    override suspend fun setHapticsEnabled(value: Boolean) {
        context.hcn2048DataStore.edit { it[Keys.HAPTICS] = value }
    }

    override suspend fun setAdsRemoved(value: Boolean) {
        context.hcn2048DataStore.edit { it[Keys.ADS_REMOVED] = value }
    }

    override suspend fun setBestScore(value: Int) {
        context.hcn2048DataStore.edit { it[Keys.BEST_SCORE] = value }
    }

    override suspend fun saveGame(value: String?) {
        context.hcn2048DataStore.edit { prefs ->
            if (value == null) {
                prefs.remove(Keys.SAVED_GAME)
            } else {
                prefs[Keys.SAVED_GAME] = value
            }
        }
    }

    override suspend fun setGameOverCount(value: Int) {
        context.hcn2048DataStore.edit { it[Keys.GAME_OVER_COUNT] = value }
    }

    override suspend fun setReviewRequested(value: Boolean) {
        context.hcn2048DataStore.edit { it[Keys.REVIEW_REQUESTED] = value }
    }

    override suspend fun setFreeUndoUsed(value: Boolean) {
        context.hcn2048DataStore.edit { it[Keys.FREE_UNDO_USED] = value }
    }
}
