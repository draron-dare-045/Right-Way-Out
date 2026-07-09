package com.example.rightway_out.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Home)
    data object Messages : BottomNavItem("messages", "Messages", Icons.Default.Chat)
    data object Shopping : BottomNavItem("shopping", "Shopping", Icons.Default.ShoppingCart)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}