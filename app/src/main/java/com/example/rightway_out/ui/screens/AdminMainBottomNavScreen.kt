package com.example.rightway_out.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rightway_out.navigation.AdminNavItem
import com.example.rightway_out.navigation.Routes
import com.example.rightway_out.ui.theme.Gold
import com.example.rightway_out.ui.theme.Maroon700
import com.example.rightway_out.ui.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminMainBottomNavScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val items = listOf(
        AdminNavItem.Dashboard,
        AdminNavItem.Messages,
        AdminNavItem.Reports,
        AdminNavItem.Settings
    )

    var currentRoute by rememberSaveable {
        mutableStateOf(AdminNavItem.Dashboard.route)
    }

    val unreadMessages = rememberAdminTotalUnreadCount()

    val doSignOut: () -> Unit = {
        FirebaseAuth.getInstance().signOut()
        navController.navigate("login") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                        },
                        icon = {
                            if (item.route == AdminNavItem.Messages.route && unreadMessages > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Maroon700, contentColor = androidx.compose.ui.graphics.Color.White) {
                                            Text(if (unreadMessages > 99) "99+" else unreadMessages.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = {
                            Text(text = item.title, style = MaterialTheme.typography.labelMedium)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Maroon700,
                            selectedTextColor = Maroon700,
                            indicatorColor = Gold.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentRoute) {

                AdminNavItem.Dashboard.route -> {
                    AdminScreen(
                        onLogout = doSignOut,
                        onAddStudent = {
                            navController.navigate(Routes.ADD_STUDENT)
                        },
                        onStudentClick = { student ->
                            navController.navigate(Routes.studentProfile(student.id))
                        },
                        onMessageStudent = { student ->
                            navController.navigate(Routes.messaging(student.id, student.name))
                        },
                        themeViewModel = themeViewModel
                    )
                }

                AdminNavItem.Messages.route -> {
                    AdminMessagesScreen(
                        onBack = {
                            currentRoute = AdminNavItem.Dashboard.route
                        },
                        onOpenChat = { sid, name ->
                            navController.navigate(Routes.messaging(sid, name))
                        }
                    )
                }

                AdminNavItem.Reports.route -> {
                    AdminReportsScreen(
                        onStudentClick = { student ->
                            navController.navigate(Routes.studentProfile(student.id))
                        }
                    )
                }

                AdminNavItem.Settings.route -> {
                    AdminSettingsScreen(
                        themeViewModel = themeViewModel,
                        onLogout = doSignOut
                    )
                }
            }
        }
    }
}