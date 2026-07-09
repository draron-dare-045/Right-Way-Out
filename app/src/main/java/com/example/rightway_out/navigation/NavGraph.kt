package com.example.rightway_out.navigation

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.rightway_out.ui.screens.*
import com.example.rightway_out.ui.viewmodel.AuthViewModel
import com.example.rightway_out.ui.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object Routes {
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val MAIN = "main"                    // ← Student Bottom Nav Container
    const val ADMIN = "admin"                   // ← Admin Dashboard
    const val ADD_STUDENT = "add_student"
    const val ADMIN_MESSAGES = "admin_messages"
    const val STUDENT_PROFILE = "student_profile/{studentId}"
    const val MESSAGING = "messaging/{studentId}/{studentName}"

    fun studentProfile(studentId: String) = "student_profile/$studentId"
    fun messaging(studentId: String, studentName: String) =
        "messaging/$studentId/${Uri.encode(studentName)}"
}

@Composable
fun RightWayOutNavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.LANDING,
        enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(320)) { it / 6 } },
        exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(320)) { -it / 10 } },
        popEnterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(320)) { -it / 10 } },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(320)) { it / 6 } }
    ) {
        // Landing + Session Check
        composable(Routes.LANDING) {
            var checked by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val user = auth.currentUser
                if (user != null) {
                    scope.launch {
                        try {
                            val doc = FirebaseFirestore.getInstance()
                                .collection("students").document(user.uid).get().await()
                            val role = doc.getString("role") ?: "STUDENT"
                            val destination = if (role == "ADMIN") Routes.ADMIN else Routes.MAIN

                            navController.navigate(destination) {
                                popUpTo(Routes.LANDING) { inclusive = true }
                            }
                        } catch (_: Exception) {
                            checked = true
                        }
                    }
                } else {
                    checked = true
                }
            }

            if (checked) {
                LandingScreen(onGetStarted = { navController.navigate(Routes.LOGIN) })
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { _, isAdmin ->
                val destination = if (isAdmin) Routes.ADMIN else Routes.MAIN
                navController.navigate(destination) {
                    popUpTo(Routes.LANDING) { inclusive = true }
                }
            })
        }

        // ==================== STUDENT: MAIN BOTTOM NAVIGATION ====================
        composable(Routes.MAIN) {
            MainBottomNavScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        // ==================== ADMIN: DASHBOARD ====================
        composable(Routes.ADMIN) {
            AdminScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                onAddStudent = {
                    navController.navigate(Routes.ADD_STUDENT)
                },
                onStudentClick = { student ->
                    navController.navigate(Routes.studentProfile(student.id))
                },
                onMessageStudent = { student ->
                    navController.navigate(Routes.messaging(student.id, student.name))
                },
                onOpenMessages = {
                    navController.navigate(Routes.ADMIN_MESSAGES)
                },
                themeViewModel = themeViewModel
            )
        }

        // Other screens (still accessible via deep links if needed)
        composable(Routes.ADD_STUDENT) {
            AddStudentScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MESSAGES) {
            AdminMessagesScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { sid, name ->
                    navController.navigate(Routes.messaging(sid, name))
                }
            )
        }

        // Admin viewing an individual student's profile
        composable(
            route = Routes.STUDENT_PROFILE,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId").orEmpty()
            StudentProfileScreen(
                studentId = studentId,
                isAdminView = true,
                onBack = { navController.popBackStack() },
                onMessage = { studentName ->
                    navController.navigate(Routes.messaging(studentId, studentName))
                }
            )
        }

        // Shared messaging thread (used by both admin and student flows)
        composable(
            route = Routes.MESSAGING,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("studentName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId").orEmpty()
            val studentName = backStackEntry.arguments?.getString("studentName").orEmpty()
            MessagingScreen(
                studentId = studentId,
                studentName = studentName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}