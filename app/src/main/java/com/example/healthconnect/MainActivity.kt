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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.healthconnect.navigation.AppNavGraph
import com.example.healthconnect.ui.theme.HealthConnectTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

import com.example.healthconnect.uiscreen.HealthScreen
import com.example.healthconnect.uiscreen.HealthScreenViewModel
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

//                // создаём ViewModel
//                val viewModel = remember {
//                    HealthScreenViewModel(healthManager)
//                }

                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    healthManager = healthManager
                )

//                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
//                    HealthScreen(
//                        steps = viewModel.stepsText,
//                        heartRateCount = viewModel.heartRateText,
//                        sleepTime = viewModel.sleepTimeText,
//                        error = viewModel.errorText,
//                        selectedDate = viewModel.selectedDate,
//                        onPrevDay = viewModel::prevDay,
//                        onNextDay = viewModel::nextDay,
//                        onEditClick = {
//                            navController.navigate("edit")
//                        },
//                        modifier = Modifier.padding(padding)
//                    )
//                }
            }
        }

        lifecycleScope.launch {
            val grantedPermissions =
                healthConnectClient.permissionController.getGrantedPermissions()

            if (!grantedPermissions.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                requestPermissionsLauncher.launch(HEALTH_CONNECT_PERMISSIONS)
            } else {
//                healthManager.insertSteps()
//                healthManager.insertHeartRate()
//                healthManager.insertSleepSession()
            }
        }
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
