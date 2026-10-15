package com.calendariolaboral.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calendariolaboral.core.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    /** null hasta que el usuario elige su comunidad. */
    val region: Region? = null,
    val localHolidays: Set<LocalDate> = emptySet(),
    val vacationDays: Int = 22,
)

class SettingsRepository(private val store: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                region = Region.entries.firstOrNull { it.name == prefs[REGION] },
                localHolidays = prefs[LOCAL_HOLIDAYS].orEmpty()
                    .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                    .toSet(),
                vacationDays = (prefs[VACATION_DAYS] ?: 22).coerceIn(1, MAX_VACATION_DAYS),
            )
        }

    suspend fun setRegion(region: Region) {
        store.edit { it[REGION] = region.name }
    }

    suspend fun toggleLocalHoliday(date: LocalDate) {
        store.edit { prefs ->
            val current = prefs[LOCAL_HOLIDAYS].orEmpty()
            val key = date.toString()
            prefs[LOCAL_HOLIDAYS] = if (key in current) current - key else current + key
        }
    }

    suspend fun setVacationDays(days: Int) {
        store.edit { it[VACATION_DAYS] = days.coerceIn(1, MAX_VACATION_DAYS) }
    }

    companion object {
        const val MAX_VACATION_DAYS = 30
        private val REGION = stringPreferencesKey("region")
        private val LOCAL_HOLIDAYS = stringSetPreferencesKey("local_holidays")
        private val VACATION_DAYS = intPreferencesKey("vacation_days")
    }
}
