package com.example.healthconnect.uiscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEditClick,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit health data"
                )
            }
        }
    ) { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Заголовок
                Text(
                    text = "Health data",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Навигация по дате
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onPrevDay) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Previous day"
                        )
                    }


                    Text(
                        text = selectedDate.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(onClick = onNextDay) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Next day"
                        )
                    }

                }

                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Карточки данных
                HealthDataCard(
                    title = "Steps",
                    value = steps,
                    unit = "steps"
                )

                HealthDataCard(
                    title = "Heart rate",
                    value = heartRateCount,
                    unit = "bpm"
                )

                HealthDataCard(
                    title = "Sleep",
                    value = sleepTime,
                    unit = ""
                )
            }
    }
}


@Composable
fun HealthDataCard(
    title: String,
    value: String,
    unit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (unit.isNotEmpty()) "$value $unit" else value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
