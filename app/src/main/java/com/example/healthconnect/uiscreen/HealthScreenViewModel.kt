package com.example.healthconnect.uiscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.HealthManager
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthScreenViewModel(
    private val healthManager: HealthManager
) : ViewModel() {

    var stepsText by mutableStateOf("—")
        private set

    var heartRateText by mutableStateOf("—")
        private set

    var sleepTimeText by mutableStateOf("—")
        private set

    var errorText by mutableStateOf<String?>(null)
        private set

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    fun loadDataForSelectedDate() {
        viewModelScope.launch {
            try {
                val (start, end) = dayToRange(selectedDate)

                val steps = healthManager.aggregateSteps(start, end)
                val hr = healthManager.readHeartRate(start, end)
                val sleep = healthManager.readSleepSessions(start, end)

                stepsText = steps?.toString() ?: "0"
                heartRateText =
                    calculateAverageHeartRate(hr)?.toString() ?: "—"

                val lastSleep = sleep.maxByOrNull { it.endTime }
                sleepTimeText =
                    lastSleep?.let { formatSleepPeriod(it) } ?: "—"

            } catch (e: Exception) {
                errorText = "Не удалось загрузить данные"
            }
        }
    }

    fun prevDay() {
        selectedDate = selectedDate.minusDays(1)
        loadDataForSelectedDate()
    }

    fun nextDay() {
        selectedDate = selectedDate.plusDays(1)
        loadDataForSelectedDate()
    }

    // ===== helpers =====

    private fun calculateAverageHeartRate(
        records: List<HeartRateRecord>
    ): Int? {
        val values = records.flatMap {
            it.samples.map { sample -> sample.beatsPerMinute.toInt() }
        }
        return if (values.isNotEmpty()) values.sum() / values.size else null
    }

    private fun dayToRange(date: LocalDate): Pair<Instant, Instant> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        return start to end
    }

    private fun formatSleepPeriod(session: SleepSessionRecord): String {
        val duration = Duration.between(session.startTime, session.endTime)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return "${hours} ч ${minutes} мин"
    }
}
