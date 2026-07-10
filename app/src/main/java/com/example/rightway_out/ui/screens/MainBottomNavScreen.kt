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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rightway_out.navigation.BottomNavItem
import com.example.rightway_out.ui.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

// Brand colors
private val Maroon700 = Color(0xFF8B0000)
private val Gold = Color(0xFFFFD700)

@Composable
fun MainBottomNavScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Messages,
        BottomNavItem.Shopping,
        BottomNavItem.Profile
    )

    var currentRoute by rememberSaveable {
        mutableStateOf(BottomNavItem.Dashboard.route)
    }

    val unreadMessages = rememberChatUnreadCount(
        studentId = getCurrentStudentId(),
        viewerIsAdmin = false
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                        },
                        icon = {
                            if (item.route == BottomNavItem.Messages.route && unreadMessages > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Maroon700) {
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
                            Text(text = item.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Maroon700,
                            selectedTextColor = Maroon700,
                            indicatorColor = Gold.copy(alpha = 0.15f)
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

                BottomNavItem.Dashboard.route -> {
                    StudentDashboard(
                        studentId = getCurrentStudentId(),
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()

                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        themeViewModel = themeViewModel
                    )
                }

                BottomNavItem.Messages.route -> {
                    MessagingScreen(
                        studentId = getCurrentStudentId(),
                        studentName = "Admin/Support",
                        onBack = {
                            currentRoute = BottomNavItem.Dashboard.route
                        }
                    )
                }

                BottomNavItem.Shopping.route -> {
                    ShoppingListScreen(
                        studentId = getCurrentStudentId(),
                        onBack = {
                            currentRoute = BottomNavItem.Dashboard.route
                        }
                    )
                }

                BottomNavItem.Profile.route -> {
                    StudentProfileScreen(
                        studentId = getCurrentStudentId(),
                        onBack = {
                            // No back action needed for bottom navigation
                        },
                        onMessage = {
                            currentRoute = BottomNavItem.Messages.route
                        }
                    )
                }
            }
        }
    }
}

/**
 * Returns the currently authenticated student's Firebase UID.
 */
private fun getCurrentStudentId(): String {
    return FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
}