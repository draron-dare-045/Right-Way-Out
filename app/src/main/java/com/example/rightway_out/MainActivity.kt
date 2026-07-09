package com.example.rightway_out

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.rightway_out.navigation.RightWayOutNavGraph
import com.example.rightway_out.ui.theme.KapsabetTheme
import com.example.rightway_out.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash BEFORE super.onCreate — eliminates white flash
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            KapsabetTheme(darkTheme = isDarkMode) {
                Surface {
                    RightWayOutNavGraph(
                        navController   = rememberNavController(),
                        themeViewModel  = themeViewModel
                    )
                }
            }
        }
    }
}
