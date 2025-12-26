package com.example.healthconnect.uiscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditHealthScreen(
    steps: String,
    heartRate: String,
    sleepHours: String,
    onStepsChange: (String) -> Unit,
    onHeartRateChange: (String) -> Unit,
    onSleepChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Edit health data", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = steps,
                onValueChange = onStepsChange,
                label = { Text("Steps") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )


            OutlinedTextField(
                value = heartRate,
                onValueChange = onHeartRateChange,
                label = { Text("Heart rate") }
            )

            OutlinedTextField(
                value = sleepHours,
                onValueChange = onSleepChange,
                label = { Text("Sleep (hours)") }
            )

            Button(onClick = onSaveClick) {
                Text("Save")
            }
        }
    }
}
