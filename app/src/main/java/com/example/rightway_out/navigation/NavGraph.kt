package com.example.rightway_out.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.rightway_out.ui.screens.*
import com.example.rightway_out.ui.viewmodel.AuthViewModel

object Routes {
    const val LOGIN        = "login"
    const val STUDENT      = "student/{studentId}"
    const val ADMIN        = "admin"
    const val ADD_STUDENT  = "add_student"
    const val SHOPPING     = "shopping/{studentId}"
    fun studentRoute(uid: String)  = "student/$uid"
    fun shoppingRoute(uid: String) = "shopping/$uid"
}

@Composable
fun RightWayOutNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val startDestination = if (authState.isLoggedIn)
        Routes.studentRoute(authViewModel.currentUser?.uid ?: "") else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { uid, isAdmin ->
                val dest = if (isAdmin) Routes.ADMIN else Routes.studentRoute(uid)
                navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
            })
        }

        composable(Routes.STUDENT,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { back ->
            val studentId = back.arguments?.getString("studentId") ?: ""
            StudentDashboard(
                studentId = studentId,
                onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                onOpenShoppingList = { navController.navigate(Routes.shoppingRoute(studentId)) }
            )
        }

        composable(Routes.ADMIN) {
            AdminScreen(
                onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                onAddStudent = { navController.navigate(Routes.ADD_STUDENT) }
            )
        }

        composable(Routes.ADD_STUDENT) {
            AddStudentScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SHOPPING,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { back ->
            val studentId = back.arguments?.getString("studentId") ?: ""
            ShoppingListScreen(
                studentId = studentId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
