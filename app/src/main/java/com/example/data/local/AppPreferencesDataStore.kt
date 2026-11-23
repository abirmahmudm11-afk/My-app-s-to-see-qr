package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scan2money_prefs")

data class UserPreferencesState(
    val onboardingCompleted: Boolean = false,
    val guestMode: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val reducedMotionEnabled: Boolean = false,
    val marketingConsent: Boolean = false,
    val countryCode: String = "US",
    val languageCode: String = "en"
)

class AppPreferencesDataStore(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val GUEST_MODE = booleanPreferencesKey("guest_mode")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val MARKETING_CONSENT = booleanPreferencesKey("marketing_consent")
        val COUNTRY_CODE = stringPreferencesKey("country_code")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.dataStore.data.map { prefs ->
        UserPreferencesState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            guestMode = prefs[Keys.GUEST_MODE] ?: false,
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: false,
            reducedMotionEnabled = prefs[Keys.REDUCED_MOTION] ?: false,
            marketingConsent = prefs[Keys.MARKETING_CONSENT] ?: false,
            countryCode = prefs[Keys.COUNTRY_CODE] ?: "US",
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en"
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setGuestMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GUEST_MODE] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setMarketingConsent(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MARKETING_CONSENT] = enabled }
    }

    suspend fun setCountryAndLanguage(countryCode: String, languageCode: String) {
        context.dataStore.edit {
            it[Keys.COUNTRY_CODE] = countryCode
            it[Keys.LANGUAGE_CODE] = languageCode
        }
    }
}
