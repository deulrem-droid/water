package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dailyHealthDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "senior_daily_health_prefs")

interface DailyHealthRepository {
  fun observeTodayRecord(): Flow<DailyRecord>

  suspend fun toggleMedication(slot: MedicationSlot)

  suspend fun incrementWater()

  suspend fun decrementWater()

  suspend fun ensureCurrentDate()
}

class DataStoreDailyHealthRepository(
  private val dataStore: DataStore<Preferences>,
  private val dateProvider: DateProvider,
) : DailyHealthRepository {

  private object Keys {
    val RECORD_DATE = stringPreferencesKey("record_date")
    val MORNING_TAKEN = booleanPreferencesKey("morning_taken")
    val LUNCH_TAKEN = booleanPreferencesKey("lunch_taken")
    val DINNER_TAKEN = booleanPreferencesKey("dinner_taken")
    val WATER_CUPS = intPreferencesKey("water_cups")
  }

  override fun observeTodayRecord(): Flow<DailyRecord> =
    dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        val todayKey = dateProvider.getToday().dateKey
        val storedDate = prefs[Keys.RECORD_DATE]
        if (storedDate != todayKey) {
          DailyRecord(dateKey = todayKey)
        } else {
          DailyRecord(
            dateKey = todayKey,
            morningTaken = prefs[Keys.MORNING_TAKEN] ?: false,
            lunchTaken = prefs[Keys.LUNCH_TAKEN] ?: false,
            dinnerTaken = prefs[Keys.DINNER_TAKEN] ?: false,
            waterCups = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0),
          )
        }
      }

  override suspend fun toggleMedication(slot: MedicationSlot) {
    val todayKey = dateProvider.getToday().dateKey
    dataStore.edit { prefs ->
      resetIfNewDay(prefs, todayKey)
      when (slot) {
        MedicationSlot.MORNING -> {
          val current = prefs[Keys.MORNING_TAKEN] ?: false
          prefs[Keys.MORNING_TAKEN] = !current
        }
        MedicationSlot.LUNCH -> {
          val current = prefs[Keys.LUNCH_TAKEN] ?: false
          prefs[Keys.LUNCH_TAKEN] = !current
        }
        MedicationSlot.DINNER -> {
          val current = prefs[Keys.DINNER_TAKEN] ?: false
          prefs[Keys.DINNER_TAKEN] = !current
        }
      }
    }
  }

  override suspend fun incrementWater() {
    val todayKey = dateProvider.getToday().dateKey
    dataStore.edit { prefs ->
      resetIfNewDay(prefs, todayKey)
      val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
      prefs[Keys.WATER_CUPS] = current + 1
    }
  }

  override suspend fun decrementWater() {
    val todayKey = dateProvider.getToday().dateKey
    dataStore.edit { prefs ->
      resetIfNewDay(prefs, todayKey)
      val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
      prefs[Keys.WATER_CUPS] = (current - 1).coerceAtLeast(0)
    }
  }

  override suspend fun ensureCurrentDate() {
    val todayKey = dateProvider.getToday().dateKey
    dataStore.edit { prefs -> resetIfNewDay(prefs, todayKey) }
  }

  private fun resetIfNewDay(
    prefs: androidx.datastore.preferences.core.MutablePreferences,
    todayKey: String,
  ) {
    val storedDate = prefs[Keys.RECORD_DATE]
    if (storedDate != todayKey) {
      prefs[Keys.RECORD_DATE] = todayKey
      prefs[Keys.MORNING_TAKEN] = false
      prefs[Keys.LUNCH_TAKEN] = false
      prefs[Keys.DINNER_TAKEN] = false
      prefs[Keys.WATER_CUPS] = 0
    }
  }
}
