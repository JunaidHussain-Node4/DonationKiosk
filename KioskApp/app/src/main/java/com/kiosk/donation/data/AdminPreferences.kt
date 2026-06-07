package com.kiosk.donation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kiosk_prefs")

class AdminPreferences(private val context: Context) {

    companion object {
        private val KEY_ADMIN_PIN       = stringPreferencesKey("admin_pin")
        private val KEY_SUMUP_AFFILIATE = stringPreferencesKey("sumup_affiliate_key")
        private val KEY_ORG_NAME        = stringPreferencesKey("org_name")
        private val KEY_DONATIONS_ENABLED = booleanPreferencesKey("donations_enabled")
        private val KEY_PRODUCTS_ENABLED  = booleanPreferencesKey("products_enabled")
        private val KEY_BASKET_TIMEOUT    = stringPreferencesKey("basket_timeout_minutes")

        /** Default PIN — change immediately via the admin screen after first install. */
        const val DEFAULT_PIN = "1234"
    }

    val adminPin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ADMIN_PIN] ?: DEFAULT_PIN
    }

    val sumupAffiliateKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SUMUP_AFFILIATE] ?: "YOUR_SUMUP_AFFILIATE_KEY"
    }

    val orgName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ORG_NAME] ?: "Our Charity"
    }

    val isDonationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DONATIONS_ENABLED] ?: true
    }

    val isProductsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRODUCTS_ENABLED] ?: true
    }

    val basketTimeoutMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASKET_TIMEOUT]?.toIntOrNull() ?: 10
    }

    suspend fun setAdminPin(pin: String) {
        context.dataStore.edit { it[KEY_ADMIN_PIN] = pin }
    }

    suspend fun setSumupAffiliateKey(key: String) {
        context.dataStore.edit { it[KEY_SUMUP_AFFILIATE] = key }
    }

    suspend fun setOrgName(name: String) {
        context.dataStore.edit { it[KEY_ORG_NAME] = name }
    }

    suspend fun setDonationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DONATIONS_ENABLED] = enabled }
    }

    suspend fun setProductsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PRODUCTS_ENABLED] = enabled }
    }

    suspend fun setBasketTimeout(minutes: Int) {
        context.dataStore.edit { it[KEY_BASKET_TIMEOUT] = minutes.toString() }
    }
}
