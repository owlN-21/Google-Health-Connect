package com.example.healthconnect.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnect.HealthManager
import com.example.healthconnect.uiscreen.EditHealthScreen
import com.example.healthconnect.uiscreen.EditHealthScreenViewModel
import com.example.healthconnect.uiscreen.HealthScreen
import com.example.healthconnect.uiscreen.HealthScreenViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    healthManager: HealthManager
) {
    NavHost(
        navController = navController,
        startDestination = "health"
    ) {

        composable("health") {
            val viewModel = remember {
                HealthScreenViewModel(healthManager)
            }

            HealthScreen(
                steps = viewModel.stepsText,
                heartRateCount = viewModel.heartRateText,
                sleepTime = viewModel.sleepTimeText,
                error = viewModel.errorText,
                selectedDate = viewModel.selectedDate,
                onPrevDay = viewModel::prevDay,
                onNextDay = viewModel::nextDay,
                onEditClick = {
                    navController.navigate("edit")
                }
            )
        }

        composable("edit") {
            val viewModel = remember {
                EditHealthScreenViewModel(healthManager)
            }

            EditHealthScreen(
                steps = viewModel.steps,
                heartRate = viewModel.heartRate,
                sleepHours = viewModel.sleepHours,
                onStepsChange = viewModel::onStepsChange,
                onHeartRateChange = viewModel::onHeartRateChange,
                onSleepChange = viewModel::onSleepChange,
                onSaveClick = {
                    viewModel.save()
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
