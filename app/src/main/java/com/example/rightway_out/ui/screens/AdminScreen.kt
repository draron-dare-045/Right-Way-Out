package com.example.rightway_out.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.StudentItem
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    onAddStudent: () -> Unit,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val state by viewModel.adminState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var selectedStudent by remember { mutableStateOf<StudentModel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadAllStudents() }
    LaunchedEffect(updateState.successMessage) {
        if (updateState.successMessage != null) {
            selectedStudent = null
            viewModel.clearUpdateFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", fontWeight = FontWeight.Bold)
                        Text("Manage Clearances", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.signOut(onLogout) }) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStudent,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Student") },
                text = { Text("Add Student") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search students...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Summary chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("${state.students.size} Total") },
                    icon = { Icon(Icons.Default.Group, null, Modifier.size(16.dp)) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("${state.students.count { it.isFullyCleared }} Cleared") },
                    icon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("${state.students.count { it.hasFlaggedDepartment }} Flagged") },
                    icon = { Icon(Icons.Default.Flag, null, Modifier.size(16.dp)) }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.students.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No students yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap '+ Add Student' to get started.",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    val filtered = state.students.filter {
                        searchQuery.isBlank() ||
                        it.name.contains(searchQuery, true) ||
                        it.admissionNumber.contains(searchQuery, true)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered, key = { it.id }) { student ->
                            StudentItem(student = student, onEditClick = { selectedStudent = it })
                        }
                        item { Spacer(Modifier.height(80.dp)) } // space for FAB
                    }
                }
            }
        }
    }

    // Edit clearance dialog
    selectedStudent?.let { student ->
        EditClearanceDialog(
            student = student,
            isUpdating = updateState.isUpdating,
            errorMessage = updateState.errorMessage,
            onDismiss = { selectedStudent = null; viewModel.clearUpdateFeedback() },
            onConfirm = { dept, status, comment ->
                viewModel.updateClearance(student.id, dept, status, comment)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClearanceDialog(
    student: StudentModel,
    isUpdating: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val departments = listOf("library", "boarding", "sports", "finance")
    var selectedDept by remember { mutableStateOf(departments.first()) }
    var selectedStatus by remember { mutableStateOf("PENDING") }
    var comment by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Clearance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Student: ${student.name}", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = !deptExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDept.replaceFirstChar { it.uppercaseChar() },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deptExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept.replaceFirstChar { it.uppercaseChar() }) },
                                onClick = { selectedDept = dept; deptExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus, onValueChange = {}, readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("PENDING", "CLEARED", "FLAGGED").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { selectedStatus = s; statusExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    placeholder = { Text("e.g. Owes 2000 KSH") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onConfirm(selectedDept, selectedStatus, comment) },
                        enabled = !isUpdating
                    ) {
                        if (isUpdating)
                            CircularProgressIndicator(modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Save")
                    }
                }
            }
        }
    }
}
