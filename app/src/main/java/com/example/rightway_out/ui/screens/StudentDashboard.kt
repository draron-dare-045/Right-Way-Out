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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.DepartmentCard
import com.example.rightway_out.ui.components.SkeletonBlock
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel
import com.example.rightway_out.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    studentId: String,
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var showMoreOptions by remember { mutableStateOf(false) }

    LaunchedEffect(studentId) {
        viewModel.loadStudentDashboard(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Clearance", fontWeight = FontWeight.Bold, color = White)
                        Text("Kapsabet High School", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreOptions = !showMoreOptions }) {
                        Icon(Icons.Default.MoreVert, "More", tint = White)
                    }
                    DropdownMenu(
                        expanded = showMoreOptions,
                        onDismissRequest = { showMoreOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { showMoreOptions = false },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Help") },
                            onClick = { showMoreOptions = false },
                            leadingIcon = { Icon(Icons.Default.Help, null) }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Logout", color = Maroon700) },
                            onClick = {
                                showMoreOptions = false
                                viewModel.signOut(onLogout)
                            },
                            leadingIcon = { Icon(Icons.Default.Logout, null, tint = Maroon700) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = Cream
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.student == null -> {
                    DashboardSkeleton()
                }
                state.errorMessage != null && state.student == null -> {
                    ErrorState(
                        message = state.errorMessage!!,
                        onRetry = { viewModel.loadStudentDashboard(studentId) }
                    )
                }
                state.student != null -> {
                    DashboardContent(student = state.student!!)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.WifiOff,
            null,
            tint = Maroon700,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Connection Error",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Maroon700
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            fontSize = 14.sp,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Maroon700)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Retry", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DashboardContent(student: StudentModel) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Maroon800, Maroon700)))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Gold,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            student.name.take(2).uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = White)
                    Text("Adm: ${student.admissionNumber}", fontSize = 13.sp, color = Gold)
                    Text(student.email, fontSize = 11.sp, color = White.copy(alpha = 0.7f))
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StatusBanner(student = student)
            SectionHeader("Department Status")
            DepartmentStatusGrid(student = student)
            SectionHeader("Need Help?")
            HelpCard()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusBanner(student: StudentModel) {
    val isFlagged = student.hasFlaggedDepartment
    val isCleared = student.isFullyCleared

    val (bgColor, fgColor, icon, label, msg) = when {
        isFlagged -> BannerData(Color(0xFFFFEBEE), Maroon700, Icons.Default.Cancel, "Action Required", "One or more departments require your attention")
        isCleared -> BannerData(Color(0xFFE8F5E9), Forest, Icons.Default.CheckCircle, "Fully Cleared", "You have been cleared by all departments")
        else -> BannerData(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.HourglassEmpty, "Clearance Pending", "Your clearance is being processed")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = fgColor.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = fgColor, modifier = Modifier.padding(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, color = fgColor, fontSize = 14.sp)
                Text(msg, fontSize = 12.sp, color = fgColor.copy(alpha = 0.75f))
            }
        }
    }
}

@Composable
private fun DepartmentStatusGrid(student: StudentModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DepartmentCard("Library", Icons.Default.MenuBook, student.libraryStatus, student.libraryComment)
        DepartmentCard("Boarding", Icons.Default.Hotel, student.boardingStatus, student.boardingComment)
        DepartmentCard("Sports", Icons.Default.SportsSoccer, student.sportsStatus, student.sportsComment)
        DepartmentCard("Finance", Icons.Default.AccountBalance, student.financeStatus, student.financeComment)
    }
}

@Composable
private fun HelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clearance Tips", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
            }
            Text(
                "• Check each department's requirements\n• Keep your profile updated\n• Message admin for clarification",
                fontSize = 12.sp,
                color = Color(0xFF664D00),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Maroon700,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

private data class BannerData(
    val bg: Color,
    val fg: Color,
    val icon: ImageVector,
    val label: String,
    val msg: String
)

@Composable
private fun DashboardSkeleton() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Maroon800, Maroon700))).padding(24.dp)) {
            Row {
                SkeletonBlock(modifier = Modifier.size(72.dp), shape = CircleShape)
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(modifier = Modifier.width(160.dp).height(18.dp))
                    SkeletonBlock(modifier = Modifier.width(100.dp).height(12.dp))
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp))
            repeat(4) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(12.dp))
            }
        }
    }
}