package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRepository
import com.example.data.DailyRecord
import com.example.data.DateProvider
import com.example.data.MedicationSlot
import com.example.data.TodayDateDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface HealthUiState {
  data object Loading : HealthUiState

  data class Success(
    val dateDisplay: TodayDateDisplay,
    val record: DailyRecord,
  ) : HealthUiState
}

class MainViewModel(
  private val repository: DailyHealthRepository,
  private val dateProvider: DateProvider,
) : ViewModel() {

  private val currentDateFlow = MutableStateFlow(dateProvider.getToday())

  val uiState: StateFlow<HealthUiState> =
    combine(currentDateFlow, repository.observeTodayRecord()) { dateDisplay, record ->
        val normalizedRecord =
          if (record.dateKey == dateDisplay.dateKey) {
            record
          } else {
            DailyRecord(dateKey = dateDisplay.dateKey)
          }
        HealthUiState.Success(dateDisplay = dateDisplay, record = normalizedRecord)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HealthUiState.Loading,
      )

  init {
    viewModelScope.launch { refreshDateInternal() }
    viewModelScope.launch {
      while (isActive) {
        delay(dateProvider.millisUntilNextMidnight())
        refreshDateInternal()
      }
    }
  }

  fun onToggleMedication(slot: MedicationSlot) {
    viewModelScope.launch {
      refreshDateInternal()
      repository.toggleMedication(slot)
    }
  }

  fun onAddWaterCup() {
    viewModelScope.launch {
      refreshDateInternal()
      repository.incrementWater()
    }
  }

  fun onRemoveWaterCup() {
    viewModelScope.launch {
      refreshDateInternal()
      repository.decrementWater()
    }
  }

  fun onRefreshDate() {
    viewModelScope.launch { refreshDateInternal() }
  }

  private suspend fun refreshDateInternal() {
    val latestDate = dateProvider.getToday()
    if (currentDateFlow.value.dateKey != latestDate.dateKey) {
      currentDateFlow.value = latestDate
    }
    repository.ensureCurrentDate()
  }

  class Factory(
    private val repository: DailyHealthRepository,
    private val dateProvider: DateProvider,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
        return MainViewModel(repository, dateProvider) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
