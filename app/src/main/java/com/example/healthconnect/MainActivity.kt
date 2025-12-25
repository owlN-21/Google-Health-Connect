package com.example.healthconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.lifecycle.lifecycleScope
import com.example.healthconnect.ui.theme.HealthConnectTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter


private const val HEALTH_CONNECT_PACKAGE =
    "com.google.android.apps.healthdata"

private val HEALTH_CONNECT_PERMISSIONS = setOf(
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getWritePermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getWritePermission(StepsRecord::class),
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

        lifecycleScope.launch {
            val grantedPermissions =
                healthConnectClient.permissionController.getGrantedPermissions()

            if (!grantedPermissions.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                // Запрашиваем permissions
                requestPermissionsLauncher.launch(HEALTH_CONNECT_PERMISSIONS)
            }else{
                insertSteps(healthConnectClient)

                val endTime = Instant.now()
                val startTime = endTime.minus(Duration.ofHours(1))

                aggregateSteps(
                    healthConnectClient = healthConnectClient,
                    startTime = startTime,
                    endTime = endTime
                )

                readHeartRateByTimeRange(
                    healthConnectClient = healthConnectClient,
                    startTime = startTime,
                    endTime = endTime
                )
            }
        }


        // Проверяем доступность фичи background read
        val isBackgroundReadAvailable =
            healthConnectClient.features.getFeatureStatus(
                HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
            ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE



        enableEdgeToEdge()
        setContent {
            HealthConnectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = if (isBackgroundReadAvailable) {
                            "Background read AVAILABLE"
                        } else {
                            "Background read NOT available"
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
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

    suspend fun insertSteps(healthConnectClient: HealthConnectClient) {
        val endTime = Instant.now()
        val startTime = endTime.minus(Duration.ofMinutes(15))
        try {
            val stepsRecord = StepsRecord(
                count = 120,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                metadata = Metadata.autoRecorded(
                    device = Device(type = Device.TYPE_WATCH)
                ),
            )
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            Log.e(
                "HealthConnect",
                "Error inserting steps",
                e
            )
        }

    }

    suspend fun readHeartRateByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            Log.d(
                "HealthConnect",
                "Heart rate records count = ${response.records.size}"
            )

            for (record in response.records) {
                Log.d(
                    "HealthConnect",
                    "HeartRate: start=${record.startTime}, end=${record.endTime}, samples=${record.samples}"
                )
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading heart rate", e)
        }
    }


    suspend fun aggregateSteps(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            // The result may be null if no data is available in the time range
            val stepCount = response[StepsRecord.COUNT_TOTAL]
            Log.d(
                "HealthConnect",
                "Aggregated steps from $startTime to $endTime = $stepCount"
            )
        } catch (e: Exception) {
            // Run error handling here
            Log.e("HealthConnect", "Error aggregating steps", e)
        }
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
