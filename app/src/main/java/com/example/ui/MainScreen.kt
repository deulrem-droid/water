package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.R
import com.example.data.DailyRecord
import com.example.data.MedicationSlot
import com.example.data.TodayDateDisplay
import com.example.ui.components.MedicationCheckButton
import com.example.ui.components.WaterCupIllustration
import com.example.ui.theme.EveningMoonBg
import com.example.ui.theme.EveningMoonIndigo
import com.example.ui.theme.IvoryCardBorder
import com.example.ui.theme.IvoryHeaderSurface
import com.example.ui.theme.LunchSunBg
import com.example.ui.theme.LunchSunOrange
import com.example.ui.theme.MedDoneBadgeBg
import com.example.ui.theme.MedDoneBadgeText
import com.example.ui.theme.MedDoneGreen
import com.example.ui.theme.MorningSunAmber
import com.example.ui.theme.MorningSunBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarmInk
import com.example.ui.theme.WarmInkSecondary
import com.example.ui.theme.WaterBadgeBg
import com.example.ui.theme.WaterCardBorder
import com.example.ui.theme.WaterCardSurface
import com.example.ui.theme.WaterPrimaryBlue

@Composable
fun MainScreen(
  uiState: HealthUiState,
  onToggleMedication: (MedicationSlot) -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  onRefreshDate: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onRefreshDate() }

  val context = LocalContext.current
  val latestOnRefreshDate by rememberUpdatedState(onRefreshDate)

  DisposableEffect(context) {
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          latestOnRefreshDate()
        }
      }
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
    onDispose {
      runCatching { context.unregisterReceiver(receiver) }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    when (uiState) {
      HealthUiState.Loading -> {
        Box(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(
            color = MedDoneGreen,
            modifier = Modifier.size(56.dp),
            strokeWidth = 5.dp,
          )
        }
      }
      is HealthUiState.Success -> {
        MainContent(
          dateDisplay = uiState.dateDisplay,
          record = uiState.record,
          onToggleMedication = onToggleMedication,
          onAddWaterCup = onAddWaterCup,
          onRemoveWaterCup = onRemoveWaterCup,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
      }
    }
  }
}

@Composable
private fun MainContent(
  dateDisplay: TodayDateDisplay,
  record: DailyRecord,
  onToggleMedication: (MedicationSlot) -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier) {
    val isWideScreen = maxWidth >= 720.dp
    val scrollState = rememberScrollState()

    if (isWideScreen) {
      Row(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier.weight(1f).widthIn(max = 520.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          DateHeaderCard(dateDisplay = dateDisplay, record = record)
          MedicationSection(record = record, onToggleMedication = onToggleMedication)
        }
        Column(
          modifier = Modifier.weight(1f).widthIn(max = 520.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          WaterTrackerSection(
            waterCups = record.waterCups,
            onAddWaterCup = onAddWaterCup,
            onRemoveWaterCup = onRemoveWaterCup,
          )
        }
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        contentAlignment = Alignment.TopCenter,
      ) {
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .widthIn(max = 600.dp)
              .padding(horizontal = 20.dp, vertical = 20.dp),
          verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          DateHeaderCard(dateDisplay = dateDisplay, record = record)

          MedicationSection(record = record, onToggleMedication = onToggleMedication)

          WaterTrackerSection(
            waterCups = record.waterCups,
            onAddWaterCup = onAddWaterCup,
            onRemoveWaterCup = onRemoveWaterCup,
          )

          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
private fun DateHeaderCard(
  dateDisplay: TodayDateDisplay,
  record: DailyRecord,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth().testTag("today_date_header"),
    shape = RoundedCornerShape(26.dp),
    color = IvoryHeaderSurface,
    border = BorderStroke(width = 1.5.dp, color = IvoryCardBorder),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = null,
          tint = MedDoneGreen,
          modifier = Modifier.size(28.dp),
        )
        Text(
          text = stringResource(R.string.today_label),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MedDoneGreen,
        )
      }

      Text(
        text = "${dateDisplay.fullDateText} ${dateDisplay.dayOfWeekText}",
        style = MaterialTheme.typography.displayMedium,
        color = WarmInk,
      )

      Surface(
        modifier = Modifier.testTag("med_summary_badge"),
        shape = RoundedCornerShape(14.dp),
        color = if (record.allMedicationsTaken) MedDoneBadgeBg else PureWhite,
        border =
          BorderStroke(
            width = 1.dp,
            color = if (record.allMedicationsTaken) MedDoneGreen else IvoryCardBorder,
          ),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (record.allMedicationsTaken) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = MedDoneBadgeText,
              modifier = Modifier.size(26.dp),
            )
          }
          Text(
            text =
              if (record.allMedicationsTaken) {
                stringResource(R.string.med_all_done)
              } else {
                stringResource(R.string.med_summary_format, record.medicationTakenCount)
              },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (record.allMedicationsTaken) MedDoneBadgeText else WarmInk,
          )
        }
      }
    }
  }
}

@Composable
private fun MedicationSection(
  record: DailyRecord,
  onToggleMedication: (MedicationSlot) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Text(
      text = stringResource(R.string.med_section_title),
      style = MaterialTheme.typography.headlineMedium,
      color = WarmInk,
      modifier = Modifier.padding(start = 4.dp),
    )

    MedicationCheckButton(
      title = stringResource(R.string.med_morning),
      icon = Icons.Rounded.WbTwilight,
      iconContentDescription = stringResource(R.string.cd_morning_icon),
      iconTint = MorningSunAmber,
      iconContainerColor = MorningSunBg,
      isTaken = record.morningTaken,
      onClick = { onToggleMedication(MedicationSlot.MORNING) },
      testTag = "morning_med_button",
    )

    MedicationCheckButton(
      title = stringResource(R.string.med_lunch),
      icon = Icons.Rounded.WbSunny,
      iconContentDescription = stringResource(R.string.cd_lunch_icon),
      iconTint = LunchSunOrange,
      iconContainerColor = LunchSunBg,
      isTaken = record.lunchTaken,
      onClick = { onToggleMedication(MedicationSlot.LUNCH) },
      testTag = "lunch_med_button",
    )

    MedicationCheckButton(
      title = stringResource(R.string.med_dinner),
      icon = Icons.Rounded.NightsStay,
      iconContentDescription = stringResource(R.string.cd_dinner_icon),
      iconTint = EveningMoonIndigo,
      iconContainerColor = EveningMoonBg,
      isTaken = record.dinnerTaken,
      onClick = { onToggleMedication(MedicationSlot.DINNER) },
      testTag = "dinner_med_button",
    )
  }
}

@Composable
private fun WaterTrackerSection(
  waterCups: Int,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val goalCups = 8

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(26.dp),
      color = WaterCardSurface,
      border = BorderStroke(width = 2.dp, color = WaterCardBorder),
      shadowElevation = 2.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.weight(1f),
          ) {
            WaterCupIllustration(
              cups = waterCups,
              goalCups = goalCups,
              contentDescriptionText = stringResource(R.string.cd_water_cup, waterCups),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = stringResource(R.string.water_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = WarmInkSecondary,
              )

              Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                AnimatedContent(
                  targetState = waterCups,
                  transitionSpec = { fadeIn() togetherWith fadeOut() },
                  label = "waterCountAnim",
                ) { count ->
                  Text(
                    text = "$count",
                    style = MaterialTheme.typography.displayLarge,
                    color = WaterPrimaryBlue,
                    modifier = Modifier.testTag("water_count_text"),
                  )
                }
                Text(
                  text = stringResource(R.string.water_cup_unit),
                  style = MaterialTheme.typography.headlineMedium,
                  color = WarmInk,
                  modifier = Modifier.padding(bottom = 6.dp),
                )
              }
            }
          }

          if (waterCups > 0) {
            OutlinedButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onRemoveWaterCup()
              },
              modifier =
                Modifier.defaultMinSize(minWidth = 84.dp, minHeight = 56.dp)
                  .testTag("remove_water_button"),
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.5.dp, WaterCardBorder),
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor = PureWhite,
                  contentColor = WaterPrimaryBlue,
                ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.cd_remove_water),
                modifier = Modifier.size(24.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = stringResource(R.string.water_minus_button),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (waterCups >= goalCups) MedDoneBadgeBg else WaterBadgeBg,
        ) {
          Text(
            text =
              if (waterCups >= goalCups) {
                stringResource(R.string.water_goal_reached)
              } else {
                stringResource(R.string.water_goal_hint)
              },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (waterCups >= goalCups) MedDoneBadgeText else WaterPrimaryBlue,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
          )
        }
      }
    }

    Button(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAddWaterCup()
      },
      modifier =
        Modifier.fillMaxWidth()
          .defaultMinSize(minHeight = 82.dp)
          .testTag("add_water_button"),
      shape = RoundedCornerShape(24.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = WaterPrimaryBlue,
          contentColor = PureWhite,
        ),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.WaterDrop,
        contentDescription = stringResource(R.string.cd_water_drop),
        modifier = Modifier.size(34.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = stringResource(R.string.water_add_button),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
      )
    }
  }
}
