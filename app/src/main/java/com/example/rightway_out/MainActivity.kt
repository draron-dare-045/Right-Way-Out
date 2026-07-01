package com.example.rightway_out

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.rightway_out.navigation.RightWayOutNavGraph
import com.example.rightway_out.ui.theme.KapsabetTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KapsabetTheme {
                Surface {
                    val navController = rememberNavController()
                    RightWayOutNavGraph(navController = navController)
                }
            }
        }
    }
}
