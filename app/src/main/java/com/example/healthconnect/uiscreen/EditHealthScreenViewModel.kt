package com.example.healthconnect.uiscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.HealthManager
import kotlinx.coroutines.launch

class EditHealthScreenViewModel(
    private val healthManager: HealthManager
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

    fun save() {
        viewModelScope.launch {
            try {
                healthManager.insertSteps()
                healthManager.insertHeartRate()
                healthManager.insertSleepSession()
            } catch (e: Exception) {
                TODO()
            }
        }
    }
}
