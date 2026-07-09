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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.ClearanceStatus
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.toColor
import com.example.rightway_out.ui.components.toLabel
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    studentId: String,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    isAdminView: Boolean = false,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val dashState by viewModel.dashboardState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var editingDept by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) { viewModel.loadStudentDashboard(studentId) }
    LaunchedEffect(updateState.successMessage) {
        if (updateState.successMessage != null) {
            editingDept = null
            viewModel.clearUpdateFeedback()
            viewModel.loadStudentDashboard(studentId)
        }
    }

    val student = dashState.student

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student?.name ?: "Student Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = { onMessage(studentId) }) {
                        Icon(Icons.Default.Message, "Message", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon700,
                    titleContentColor = White
                )
            )
        },
        containerColor = Cream
    ) { padding ->
        if (student == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Maroon700)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Maroon700, Maroon800)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(88.dp).background(Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(student.name.take(2).uppercase(), fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold, color = White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
                        Text(student.admissionNumber, fontSize = 14.sp, color = Gold)
                        Text(student.email, fontSize = 12.sp, color = White.copy(alpha = 0.65f))
                    }

                    // Overall status chip
                    val overallStatus = when {
                        student.hasFlaggedDepartment -> ClearanceStatus.FLAGGED
                        student.isFullyCleared       -> ClearanceStatus.CLEARED
                        else                         -> ClearanceStatus.PENDING
                    }
                    Surface(shape = RoundedCornerShape(20.dp),
                        color = overallStatus.toColor().copy(alpha = 0.2f)) {
                        Text(
                            "Overall: ${overallStatus.toLabel()}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = overallStatus.toColor()
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Department clearance cards — each tappable (admin only)
            Text("  Clearance by Department", fontSize = 13.sp,
                fontWeight = FontWeight.Bold, color = TextLight)
            Spacer(Modifier.height(8.dp))

            listOf(
                Triple("Library",  Icons.Default.MenuBook,      Pair(student.libraryStatus,  student.libraryComment)),
                Triple("Boarding", Icons.Default.Hotel,          Pair(student.boardingStatus, student.boardingComment)),
                Triple("Sports",   Icons.Default.SportsSoccer,  Pair(student.sportsStatus,   student.sportsComment)),
                Triple("Finance",  Icons.Default.AccountBalance, Pair(student.financeStatus,  student.financeComment)),
            ).forEach { (dept, icon, pair) ->
                val (status, comment) = pair
                EditableDeptCard(
                    department = dept,
                    icon = icon,
                    status = status,
                    comment = comment,
                    isEditable = isAdminView,
                    onEdit = { if (isAdminView) editingDept = dept }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Message button
            Button(
                onClick = { onMessage(studentId) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Maroon700)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Message, null, tint = White)
                    Text("Message ${student.name.split(" ").first()}",
                        color = White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Edit dept dialog (admin only)
    if (isAdminView) {
        editingDept?.let { dept ->
            val currentStudent = student ?: return@let
            val currentStatus = when (dept) {
                "Library"  -> currentStudent.libraryStatus
                "Boarding" -> currentStudent.boardingStatus
                "Sports"   -> currentStudent.sportsStatus
                "Finance"  -> currentStudent.financeStatus
                else       -> ClearanceStatus.PENDING
            }
            val currentComment = when (dept) {
                "Library"  -> currentStudent.libraryComment
                "Boarding" -> currentStudent.boardingComment
                "Sports"   -> currentStudent.sportsComment
                "Finance"  -> currentStudent.financeComment
                else       -> ""
            }
            DeptEditDialog(
                department = dept,
                currentStatus = currentStatus.name,
                currentComment = currentComment,
                isUpdating = updateState.isUpdating,
                errorMessage = updateState.errorMessage,
                onDismiss = { editingDept = null; viewModel.clearUpdateFeedback() },
                onConfirm = { status, comment ->
                    viewModel.updateClearance(studentId, dept, status, comment)
                }
            )
        }
    }
}

@Composable
private fun EditableDeptCard(
    department: String,
    icon: ImageVector,
    status: ClearanceStatus,
    comment: String,
    isEditable: Boolean,
    onEdit: () -> Unit
) {
    Card(
        onClick = { if (isEditable) onEdit() },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp)
                    .background(status.toColor().copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = status.toColor(), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(department, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (comment.isNotBlank()) {
                    Text(comment, fontSize = 12.sp, color = TextLight)
                } else if (isEditable) {
                    Text("Tap to update status", fontSize = 12.sp, color = TextLight)
                }
            }
            Surface(shape = RoundedCornerShape(20.dp),
                color = status.toColor().copy(alpha = 0.12f)) {
                Text(status.toLabel(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = status.toColor())
            }
            if (isEditable) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, null, tint = TextLight, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeptEditDialog(
    department: String,
    currentStatus: String,
    currentComment: String,
    isUpdating: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var comment by remember { mutableStateOf(currentComment) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(40.dp)
                        .background(Maroon700.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint = Maroon700, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Update $department", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Change clearance status", fontSize = 12.sp, color = TextLight)
                    }
                }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedStatus, onValueChange = {}, readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon700, focusedLabelColor = Maroon700)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("PENDING", "CLEARED", "FLAGGED").forEach { s ->
                            DropdownMenuItem(text = { Text(s) },
                                onClick = { selectedStatus = s; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    placeholder = { Text("e.g. Owes 2000 KSH") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon700, focusedLabelColor = Maroon700)
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextMid) }
                    Button(onClick = { onConfirm(selectedStatus, comment) },
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon700)) {
                        if (isUpdating)
                            CircularProgressIndicator(color = White,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Save Changes", color = White)
                    }
                }
            }
        }
    }
}