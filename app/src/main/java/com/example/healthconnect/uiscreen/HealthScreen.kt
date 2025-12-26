package com.example.healthconnect.uiscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HealthScreen(
    steps: String,
    heartRateCount: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Health data",
            style = MaterialTheme.typography.headlineSmall
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
            return
        }

        Text(
            text = "Steps: $steps",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Heart rate records: $heartRateCount",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}