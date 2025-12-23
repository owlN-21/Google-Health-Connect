package com.example.healthconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.lifecycle.lifecycleScope
import com.example.healthconnect.ui.theme.HealthConnectTheme
import kotlinx.coroutines.launch

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
