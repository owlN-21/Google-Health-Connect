package com.example.healthconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import com.example.healthconnect.ui.theme.HealthConnectTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

import com.example.healthconnect.uiscreen.HealthScreen
import java.time.LocalDate
import java.time.ZoneId


private const val HEALTH_CONNECT_PACKAGE =
    "com.google.android.apps.healthdata"

private val HEALTH_CONNECT_PERMISSIONS = setOf(
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getWritePermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getWritePermission(StepsRecord::class),
    HealthPermission.getReadPermission(SleepSessionRecord::class),
    HealthPermission.getWritePermission(SleepSessionRecord::class),
)

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionsLauncher:
            ActivityResultLauncher<Set<String>>

    private lateinit var healthManager: HealthManager

    // ui state
    private var stepsText by mutableStateOf("—")
    private var heartRateText by mutableStateOf("—")
    private var sleepTimeText by mutableStateOf("—")
    private var errorText by mutableStateOf<String?>(null)

    private var selectedDate by mutableStateOf(LocalDate.now())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionsLauncher =
            registerForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { }

        val healthConnectClient = getHealthConnectClient() ?: return
        healthManager = HealthManager(healthConnectClient)


        setContent {
            HealthConnectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    HealthScreen(
                        steps = stepsText,
                        heartRateCount = heartRateText,
                        sleepTime = sleepTimeText,
                        error = errorText,
                        selectedDate = selectedDate,
                        onPrevDay = {
                            selectedDate = selectedDate.minusDays(1)
                            lifecycleScope.launch {
                                loadDataForDate(selectedDate)
                            }
                        },
                        onNextDay = {
                            selectedDate = selectedDate.plusDays(1)
                            lifecycleScope.launch {
                                loadDataForDate(selectedDate)
                            }
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        lifecycleScope.launch {
            val grantedPermissions =
                healthConnectClient.permissionController.getGrantedPermissions()

            if (!grantedPermissions.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                requestPermissionsLauncher.launch(HEALTH_CONNECT_PERMISSIONS)
            } else {
                try {
                    healthManager.insertSteps()
                    healthManager.insertHeartRate()
                    healthManager.insertSleepSession()

                    loadDataForDate(selectedDate)
                } catch (e: Exception) {
                    errorText = "Не удалось загрузить данные"
                }
            }
        }
    }

    private suspend fun loadDataForDate(date: LocalDate) {
        val (start, end) = dayToRange(date)

        val steps = healthManager.aggregateSteps(start, end)
        val hr = healthManager.readHeartRate(start, end)
        val sleep = healthManager.readSleepSessions(start, end)

        stepsText = steps?.toString() ?: "0"
        heartRateText =
            calculateAverageHeartRate(hr)?.toString() ?: "—"

        val lastSleep = sleep.maxByOrNull { it.endTime }
        sleepTimeText =
            lastSleep?.let { formatSleepPeriod(it) } ?: "—"
    }

    private fun calculateAverageHeartRate(
        records: List<HeartRateRecord>
    ): Int? {
        val values = records.flatMap { record ->
            record.samples.map { it.beatsPerMinute.toInt() }
        }

        return if (values.isNotEmpty()) {
            values.sum() / values.size
        } else null
    }

    private fun dayToRange(date: LocalDate): Pair<Instant, Instant> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        return start to end
    }

    private fun formatSleepPeriod(session: SleepSessionRecord): String {
        val duration = Duration.between(
            session.startTime,
            session.endTime
        )

        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return "${hours} ч ${minutes} мин"
    }

    private fun getHealthConnectClient(): HealthConnectClient? {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(
                this,
                HEALTH_CONNECT_PACKAGE
            )

        return when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                null
            }

            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                val uriString =
                    "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"

                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        setPackage("com.android.vending")
                        data = Uri.parse(uriString)
                        putExtra("overlay", true)
                        putExtra("callerId", packageName)
                    }
                )
                null
            }

            else -> HealthConnectClient.getOrCreate(this)
        }
    }

}
