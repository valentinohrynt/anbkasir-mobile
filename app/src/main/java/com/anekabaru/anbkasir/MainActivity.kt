package com.anekabaru.anbkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.anekabaru.anbkasir.ui.AppNavigation
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.AnbKasirTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnbKasirTheme {
                // Gunakan hiltViewModel() untuk mendapatkan instance yang dikelola Hilt
                val viewModel: PosViewModel = hiltViewModel()

                // Panggil sync saat aplikasi pertama kali dibuka
                LaunchedEffect(Unit) {
                    viewModel.sync()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Teruskan viewModel yang sama ke navigasi
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}