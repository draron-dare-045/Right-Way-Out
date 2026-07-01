package com.example.rightway_out.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.DepartmentCard
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    studentId: String,
    onLogout: () -> Unit,
    onOpenShoppingList: () -> Unit,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    LaunchedEffect(studentId) { viewModel.loadStudentDashboard(studentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RightWay Out", fontWeight = FontWeight.Bold)
                        Text("Kapsabet High School", fontSize = 12.sp,
                            color = White.copy(alpha = 0.75f))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.signOut(onLogout) }) {
                        Icon(Icons.Default.Logout, "Logout", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaroonPrimary,
                    titleContentColor = White
                )
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.student == null ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center),
                        color = MaroonPrimary)
                state.errorMessage != null && state.student == null ->
                    Column(modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WifiOff, null, tint = MaroonPrimary,
                            modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.errorMessage!!, color = MaroonPrimary)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadStudentDashboard(studentId) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)) {
                            Text("Retry")
                        }
                    }
                state.student != null ->
                    DashboardContent(student = state.student!!,
                        onOpenShoppingList = onOpenShoppingList)
            }
        }
    }
}

@Composable
private fun DashboardContent(student: StudentModel, onOpenShoppingList: () -> Unit) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Student header ─────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaroonPrimary)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(student.name.take(2).uppercase(), fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold, color = White)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = White)
                    Text("Adm: ${student.admissionNumber}", fontSize = 13.sp,
                        color = White.copy(alpha = 0.8f))
                    Text(student.email, fontSize = 12.sp, color = White.copy(alpha = 0.6f))
                }
            }
        }

        // ── Overall status banner ──────────────────────────────────────────
        val (bg, fg, icon, label, msg) = when {
            student.hasFlaggedDepartment -> BannerData(
                Color(0xFFFFEBEE), MaroonPrimary, Icons.Default.Cancel,
                "Action Required", "One or more departments require your attention.")
            student.isFullyCleared -> BannerData(
                Color(0xFFE8F5E9), ForestGreen, Icons.Default.CheckCircle,
                "Fully Cleared", "You have been cleared by all departments.")
            else -> BannerData(
                Color(0xFFFFF8E1), Color(0xFFE65100), Icons.Default.HourglassEmpty,
                "Clearance Pending", "Your clearance is being processed.")
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = bg)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = fg, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(label, fontWeight = FontWeight.Bold, color = fg)
                    Text(msg, fontSize = 12.sp, color = fg.copy(alpha = 0.8f))
                }
            }
        }

        // ── Department clearances ──────────────────────────────────────────
        Text("Clearance Status", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaroonPrimary)

        DepartmentCard("Library",  Icons.Default.MenuBook,      student.libraryStatus,  student.libraryComment)
        DepartmentCard("Boarding", Icons.Default.Hotel,          student.boardingStatus, student.boardingComment)
        DepartmentCard("Sports",   Icons.Default.SportsSoccer,  student.sportsStatus,   student.sportsComment)
        DepartmentCard("Finance",  Icons.Default.AccountBalance, student.financeStatus,  student.financeComment)

        // ── Shopping List card ─────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        Card(
            onClick = onOpenShoppingList,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaroonDark)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp)
                    .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingCart, null, tint = GoldAccent,
                        modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Shopping List", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = White)
                    Text("Track items needed for next term", fontSize = 12.sp,
                        color = White.copy(alpha = 0.7f))
                }
                Icon(Icons.Default.ChevronRight, null, tint = GoldAccent)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private data class BannerData(
    val bg: Color, val fg: Color, val icon: ImageVector,
    val label: String, val msg: String
)
private operator fun BannerData.component1() = bg
private operator fun BannerData.component2() = fg
private operator fun BannerData.component3() = icon
private operator fun BannerData.component4() = label
private operator fun BannerData.component5() = msg
