package com.example.rightway_out.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : AdminNavItem("admin_dashboard", "Dashboard", Icons.Default.Home)
    data object Messages : AdminNavItem("admin_messages_tab", "Messages", Icons.Default.Chat)
}