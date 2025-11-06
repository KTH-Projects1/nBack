package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val N_VALUE = intPreferencesKey("n_value")
        val NUMBER_OF_EVENTS = intPreferencesKey("number_of_events")
        val EVENT_INTERVAL = longPreferencesKey("event_interval")
        val GRID_SIZE = intPreferencesKey("grid_size")
        val NUMBER_OF_LETTERS = intPreferencesKey("number_of_letters")
        const val TAG = "SettingsRepository"
    }

    val gameSettings: Flow<GameSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading settings", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            GameSettings(
                nValue = preferences[N_VALUE] ?: 2,
                numberOfEvents = preferences[NUMBER_OF_EVENTS] ?: 20,
                eventInterval = preferences[EVENT_INTERVAL] ?: 2000L,
                gridSize = preferences[GRID_SIZE] ?: 3,
                numberOfLetters = preferences[NUMBER_OF_LETTERS] ?: 8
            )
        }

    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[N_VALUE] = settings.nValue
            preferences[NUMBER_OF_EVENTS] = settings.numberOfEvents
            preferences[EVENT_INTERVAL] = settings.eventInterval
            preferences[GRID_SIZE] = settings.gridSize
            preferences[NUMBER_OF_LETTERS] = settings.numberOfLetters
        }
        Log.d(TAG, "Settings saved: $settings")
    }
}