package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MedicationSlot {
  MORNING,
  LUNCH,
  DINNER,
}

data class DailyRecord(
  val dateKey: String,
  val morningTaken: Boolean = false,
  val lunchTaken: Boolean = false,
  val dinnerTaken: Boolean = false,
  val waterCups: Int = 0,
) {
  val medicationTakenCount: Int
    get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

  val allMedicationsTaken: Boolean
    get() = morningTaken && lunchTaken && dinnerTaken
}

data class TodayDateDisplay(
  val dateKey: String,
  val fullDateText: String,
  val dayOfWeekText: String,
)

interface DateProvider {
  fun getToday(): TodayDateDisplay

  fun millisUntilNextMidnight(): Long
}

class SystemDateProvider : DateProvider {
  override fun getToday(): TodayDateDisplay {
    val now = Calendar.getInstance().time
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dateFormatter = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
    val dayFormatter = SimpleDateFormat("EEEE", Locale.KOREA)
    return TodayDateDisplay(
      dateKey = keyFormatter.format(now),
      fullDateText = dateFormatter.format(now),
      dayOfWeekText = dayFormatter.format(now),
    )
  }

  override fun millisUntilNextMidnight(): Long {
    val now = Calendar.getInstance()
    val nextMidnight =
      (now.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 100)
      }
    return (nextMidnight.timeInMillis - now.timeInMillis).coerceAtLeast(1_000L)
  }
}
