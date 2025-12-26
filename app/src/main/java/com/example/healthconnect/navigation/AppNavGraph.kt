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
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController,
    healthManager: HealthManager
) {
    val healthViewModel  = remember {
        HealthScreenViewModel(healthManager)
    }

    NavHost(
        navController = navController,
        startDestination = "health"
    ) {
        composable("health") {
            HealthScreen(
                steps = healthViewModel .stepsText,
                heartRateCount = healthViewModel .heartRateText,
                sleepTime = healthViewModel .sleepTimeText,
                error = healthViewModel .errorText,
                selectedDate = healthViewModel .selectedDate,
                onPrevDay = healthViewModel ::prevDay,
                onNextDay = healthViewModel ::nextDay,
                onEditClick = {
                    navController.navigate("edit/${healthViewModel.selectedDate}")
                },
                onRefresh = {
                    healthViewModel .loadDataForSelectedDate()
                }
            )
        }

        composable("edit/{date}") { backStackEntry ->
            val date = LocalDate.parse(backStackEntry.arguments?.getString("date")!!)

            val editViewModel = remember {
                EditHealthScreenViewModel(
                    healthManager = healthManager,
                    date = date,
                    initialSteps = healthViewModel.stepsText,
                    initialHeartRate = healthViewModel.heartRateText,
                    initialSleep = healthViewModel.sleepTimeText
                )
            }


            EditHealthScreen(
                steps = editViewModel .steps,
                heartRate = editViewModel .heartRate,
                sleepHours = editViewModel .sleepHours,
                onStepsChange = editViewModel ::onStepsChange,
                onHeartRateChange = editViewModel ::onHeartRateChange,
                onSleepChange = editViewModel ::onSleepChange,
                onSaveClick = {
                    editViewModel.save {
                        healthViewModel.loadDataForSelectedDate()
                        navController.navigateUp()
                    }

                },
                onDeleteClick = {
                    editViewModel.deleteSteps {
                        healthViewModel.loadDataForSelectedDate()
                        navController.navigateUp()
                    }
                },
                onBackClick = {
                    navController.popBackStack(
                        route = "health",
                        inclusive = false
                    )
                }
            )
        }
    }
}
