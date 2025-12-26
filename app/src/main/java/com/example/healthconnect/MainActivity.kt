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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Launcher для запроса permissions
        requestPermissionsLauncher =
            registerForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { granted ->
                if (granted.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                    // Permissions successfully granted
                } else {
                    // Lack of required permissions
                }
            }

        // Получаем HealthConnectClient
        val healthConnectClient = getHealthConnectClient() ?: return

        val healthManager = HealthManager(healthConnectClient)

        // ui state
        var stepsText by mutableStateOf("—")
        var heartRateText by mutableStateOf("—")
        var errorText by mutableStateOf<String?>(null)
        var sleepTimeText by mutableStateOf("—")


        // permissions + data load
        lifecycleScope.launch {
            val grantedPermissions =
                healthConnectClient.permissionController.getGrantedPermissions()

            if (!grantedPermissions.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                // Запрашиваем permissions
                requestPermissionsLauncher.launch(HEALTH_CONNECT_PERMISSIONS)
            }else{
                try {
                    healthManager.insertSteps()

                    healthManager.insertHeartRate()

                    healthManager.insertSleepSession()

                    val end = Instant.now()
                    val start = end.minus(Duration.ofDays(1))

                    val steps = healthManager.aggregateSteps(start, end)
                    val hr = healthManager.readHeartRate(start, end)


                    val sleepTime = healthManager.readSleepSessions(start, end)

                    val lastSleep  = sleepTime.maxByOrNull { it.endTime }

                    sleepTimeText =
                        if (sleepTime.isNotEmpty()) {
                            formatSleepPeriod(lastSleep!!)
                        } else {
                            "—"
                        }


                    stepsText = steps?.toString() ?: "0"

                    // среднее сердцебиение за период
                    val averageHeartRate = calculateAverageHeartRate(hr)

                    heartRateText =
                        averageHeartRate?.toString()?: "—"


                } catch (e: Exception) {
                    errorText = "Не удалось загрузить данные"
                }
            }

            // Compose
            setContent {
                HealthConnectTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        HealthScreen(
                            steps = stepsText,
                            heartRateCount = heartRateText,
                            sleepTime = sleepTimeText,
                            error = errorText,
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }


        // Проверяем доступность фичи background read
        val isBackgroundReadAvailable =
            healthConnectClient.features.getFeatureStatus(
                HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
            ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE

    }

    private fun calculateAverageHeartRate(
        records: List<HeartRateRecord>
    ): Int? {

        val heartRates = mutableListOf<Int>()

        for (record in records) {
            for (sample in record.samples) {
                heartRates.add(sample.beatsPerMinute.toInt())
            }
        }

        return if (heartRates.isNotEmpty()) {
            heartRates.sum() / heartRates.size
        } else {
            null
        }
    }

    /**
     * Проверяет доступность Health Connect и
     * возвращает HealthConnectClient или null
     */
    private fun getHealthConnectClient(): HealthConnectClient? {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(this, HEALTH_CONNECT_PACKAGE)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return null
        }

        if (availabilityStatus ==
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
        ) {
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
            return null
        }

        return HealthConnectClient.getOrCreate(this)
    }

    // период сна
    fun formatSleepPeriod(session: SleepSessionRecord): String {
        val start = session.startTime
        val end = session.endTime

        val duration = Duration.between(start, end)

        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return "${hours} ч ${minutes} мин"
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HealthConnectTheme {
        Greeting("Android")
    }
}
