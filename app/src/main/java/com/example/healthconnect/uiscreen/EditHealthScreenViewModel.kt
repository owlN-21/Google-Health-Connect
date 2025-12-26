package com.example.healthconnect.uiscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.HealthManager
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditHealthScreenViewModel(
    private val healthManager: HealthManager,
    private val date: LocalDate,
    initialSteps: String,
    initialHeartRate: String,
    initialSleep: String
) : ViewModel() {


    var steps by mutableStateOf(initialSteps)
        private set

    var heartRate by mutableStateOf(initialHeartRate)
        private set

    var sleepHours by mutableStateOf(initialSleep)
        private set

    fun onStepsChange(value: String) {
        steps = value
    }

    fun onHeartRateChange(value: String) {
        heartRate = value
    }

    fun onSleepChange(value: String) {
        sleepHours = value
    }

    fun save(onFinished: () -> Unit) {
        viewModelScope.launch {
            val stepsValue = steps.toLongOrNull()

            if (stepsValue != null) {
                healthManager.insertStepsForDate(
                    stepsValue,
                    date
                )
            }

            onFinished()
        }
    }

    fun deleteSteps(onFinished: () -> Unit) {
        viewModelScope.launch {
            healthManager.deleteStepsForDate(date)
            onFinished()
        }
    }


}
