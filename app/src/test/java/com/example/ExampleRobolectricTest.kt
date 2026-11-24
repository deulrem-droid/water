package com.example

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.DateProvider
import com.example.data.MedicationSlot
import com.example.data.TodayDateDisplay
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.SeniorHealthTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  private class MutableFakeDateProvider(
    initialDateKey: String,
    var nextMidnightDelayMs: Long = 60_000L,
  ) : DateProvider {
    var currentDate =
      TodayDateDisplay(
        dateKey = initialDateKey,
        fullDateText = "2026년 11월 17일",
        dayOfWeekText = "화요일",
      )

    override fun getToday(): TodayDateDisplay = currentDate

    override fun millisUntilNextMidnight(): Long = nextMidnightDelayMs
  }

  @Test
  fun `app name matches Korean title`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("약·물 체크", appName)
  }

  @Test
  fun `repository persists medication and water across restarts and resets on new date`() =
    runTest {
      val tempFile = File.createTempFile("test_health_prefs", ".preferences_pb")
      tempFile.deleteOnExit()
      val scope1 = CoroutineScope(Dispatchers.IO + SupervisorJob())
      val dateProvider = MutableFakeDateProvider("2026-11-17")

      val ds1 = PreferenceDataStoreFactory.create(scope = scope1) { tempFile }
      val repo1 = DataStoreDailyHealthRepository(ds1, dateProvider)

      repo1.toggleMedication(MedicationSlot.MORNING)
      repo1.toggleMedication(MedicationSlot.DINNER)
      repo1.incrementWater()
      repo1.incrementWater()
      repo1.incrementWater()

      val saved1 = repo1.observeTodayRecord().first()
      assertTrue(saved1.morningTaken)
      assertFalse(saved1.lunchTaken)
      assertTrue(saved1.dinnerTaken)
      assertEquals(3, saved1.waterCups)
      scope1.cancel()

      // Simulate app restart with a fresh DataStore instance on the same file and same date
      val scope2 = CoroutineScope(Dispatchers.IO + SupervisorJob())
      val ds2 = PreferenceDataStoreFactory.create(scope = scope2) { tempFile }
      val repo2 = DataStoreDailyHealthRepository(ds2, dateProvider)

      val restored = repo2.observeTodayRecord().first()
      assertTrue(restored.morningTaken)
      assertFalse(restored.lunchTaken)
      assertTrue(restored.dinnerTaken)
      assertEquals(3, restored.waterCups)

      // Simulate next calendar day -> records reset cleanly
      dateProvider.currentDate =
        TodayDateDisplay(
          dateKey = "2026-11-18",
          fullDateText = "2026년 11월 18일",
          dayOfWeekText = "수요일",
        )
      val nextDay = repo2.observeTodayRecord().first()
      assertFalse(nextDay.morningTaken)
      assertFalse(nextDay.lunchTaken)
      assertFalse(nextDay.dinnerTaken)
      assertEquals(0, nextDay.waterCups)
      scope2.cancel()
    }

  @Test
  fun `main screen shows date medication buttons and increments water cups`() {
    val tempFile = File.createTempFile("test_ui_prefs", ".preferences_pb")
    tempFile.deleteOnExit()
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val dateProvider = MutableFakeDateProvider("2026-11-17")
    val dataStore = PreferenceDataStoreFactory.create(scope = scope) { tempFile }
    val repository = DataStoreDailyHealthRepository(dataStore, dateProvider)
    val viewModel = MainViewModel(repository, dateProvider)

    composeTestRule.setContent {
      SeniorHealthTheme {
        val state = viewModel.uiState.collectAsState().value
        MainScreen(
          uiState = state,
          onToggleMedication = viewModel::onToggleMedication,
          onAddWaterCup = viewModel::onAddWaterCup,
          onRemoveWaterCup = viewModel::onRemoveWaterCup,
          onRefreshDate = viewModel::onRefreshDate,
        )
      }
    }

    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithText("2026년 11월 17일 화요일").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("2026년 11월 17일 화요일").assertIsDisplayed()
    composeTestRule.onNodeWithTag("morning_med_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("lunch_med_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("dinner_med_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextContains("0")

    composeTestRule.onNodeWithTag("morning_med_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("복용 완료").assertIsDisplayed()

    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      runCatching {
        composeTestRule.onNodeWithTag("water_count_text").assertTextContains("2")
      }.isSuccess
    }

    composeTestRule.onNodeWithTag("remove_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      runCatching {
        composeTestRule.onNodeWithTag("water_count_text").assertTextContains("1")
      }.isSuccess
    }

    scope.cancel()
  }

  @Test
  fun `passing midnight automatically resets yesterday medication and water records in UI`() {
    val tempFile = File.createTempFile("test_midnight_prefs", ".preferences_pb")
    tempFile.deleteOnExit()
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val dateProvider = MutableFakeDateProvider("2026-11-17", nextMidnightDelayMs = 250L)
    val dataStore = PreferenceDataStoreFactory.create(scope = scope) { tempFile }
    val repository = DataStoreDailyHealthRepository(dataStore, dateProvider)
    val viewModel = MainViewModel(repository, dateProvider)

    composeTestRule.setContent {
      SeniorHealthTheme {
        val state = viewModel.uiState.collectAsState().value
        MainScreen(
          uiState = state,
          onToggleMedication = viewModel::onToggleMedication,
          onAddWaterCup = viewModel::onAddWaterCup,
          onRemoveWaterCup = viewModel::onRemoveWaterCup,
          onRefreshDate = viewModel::onRefreshDate,
        )
      }
    }

    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithText("2026년 11월 17일 화요일").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("morning_med_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()

    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isNotEmpty() &&
        runCatching {
          composeTestRule.onNodeWithTag("water_count_text").assertTextContains("1")
        }.isSuccess
    }

    // Advance fake clock past midnight while the app remains open
    dateProvider.currentDate =
      TodayDateDisplay(
        dateKey = "2026-11-18",
        fullDateText = "2026년 11월 18일",
        dayOfWeekText = "수요일",
      )
    org.robolectric.shadows.ShadowLooper.idleMainLooper(500, java.util.concurrent.TimeUnit.MILLISECONDS)
    composeTestRule.mainClock.advanceTimeBy(500)

    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithText("2026년 11월 18일 수요일").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("2026년 11월 18일 수요일").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isEmpty())
    composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextContains("0")

    scope.cancel()
  }
}
