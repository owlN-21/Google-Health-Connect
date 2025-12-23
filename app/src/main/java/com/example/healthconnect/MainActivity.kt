package com.example.healthconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import com.example.healthconnect.ui.theme.HealthConnectTheme

private const val HEALTH_CONNECT_PACKAGE =
    "com.google.android.apps.healthdata"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем HealthConnectClient или выходим
        val healthConnectClient = getHealthConnectClient() ?: return

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
