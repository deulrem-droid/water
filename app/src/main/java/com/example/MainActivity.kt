package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.SystemDateProvider
import com.example.data.dailyHealthDataStore
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.SeniorHealthTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels {
    val dateProvider = SystemDateProvider()
    val repository =
      DataStoreDailyHealthRepository(
        dataStore = applicationContext.dailyHealthDataStore,
        dateProvider = dateProvider,
      )
    MainViewModel.Factory(repository = repository, dateProvider = dateProvider)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
    )
    setContent {
      SeniorHealthTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        MainScreen(
          uiState = uiState,
          onToggleMedication = viewModel::onToggleMedication,
          onAddWaterCup = viewModel::onAddWaterCup,
          onRemoveWaterCup = viewModel::onRemoveWaterCup,
          onRefreshDate = viewModel::onRefreshDate,
        )
      }
    }
  }
}
