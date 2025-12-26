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
    private val date: LocalDate
) : ViewModel() {


    var steps by mutableStateOf("")
        private set

    var heartRate by mutableStateOf("")
        private set

    var sleepHours by mutableStateOf("")
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



}
