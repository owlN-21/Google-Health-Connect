package com.example.healthconnect.uiscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate


@Composable
fun HealthScreen(
    steps: String,
    heartRateCount: String,
    sleepTime: String,
    error: String?,
    selectedDate: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
)
 {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Health data",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Date: $selectedDate",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(onClick = { onPrevDay() }) {
            Text("← Previous day")
        }

        Button(onClick = { onNextDay() }) {
            Text("Next day →")
        }


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
            text = "Average heart rate: $heartRateCount",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Sleep time: $sleepTime",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}