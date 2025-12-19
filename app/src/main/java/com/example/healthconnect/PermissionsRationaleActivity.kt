package com.example.healthconnect


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.healthconnect.ui.theme.HealthConnectTheme

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthConnectTheme {
                Text(
                    text = """
                    Это приложение использует данные здоровья для:
                    • отображения статистики шагов и пульса
                    • анализа активности
                    • улучшения рекомендаций
                    
                    Данные не передаются третьим лицам.
                    """.trimIndent()
                )
            }
        }
    }
}