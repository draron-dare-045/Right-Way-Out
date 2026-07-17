package com.example.rightway_out.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.ClearanceStatus
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.SkeletonBlock
import com.example.rightway_out.ui.components.toColor
import com.example.rightway_out.ui.components.toLabel
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel
import com.example.rightway_out.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    onAddStudent: () -> Unit,
    onStudentClick: (StudentModel) -> Unit,
    onMessageStudent: (StudentModel) -> Unit,
    themeViewModel: ThemeViewModel,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val state      by viewModel.adminState.collectAsState()
    val isDark     by themeViewModel.isDarkMode.collectAsState()
    var searchQuery    by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { viewModel.loadAllStudents() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", style = MaterialTheme.typography.titleLarge, color = White)
                        Text("Kapsabet High School", style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = 0.75f), letterSpacing = 0.5.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { themeViewModel.toggleDarkMode() }) {
                        Icon(if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            "Toggle theme", tint = White)
                    }
                    IconButton(onClick = { viewModel.signOut(onLogout) }) {
                        Icon(Icons.Default.Logout, "Logout", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStudent,
                icon    = { Icon(Icons.Default.PersonAdd, null) },
                text    = { Text("Add Student", style = MaterialTheme.typography.labelLarge) },
                shape   = MaterialTheme.shapes.large,
                containerColor = Gold, contentColor = Maroon900
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Stats banner — a faint seal/emblem watermark nods to the fact that
            // clearance is, in effect, an official record being certified.
            Box(modifier = Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Maroon800, Maroon700)))) {
                SealWatermark(modifier = Modifier.matchParentSize())
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatChip("Total",   state.students.size.toString(), Icons.Default.Group, White)
                        StatChip("Cleared", state.students.count { it.isFullyCleared }.toString(),
                            Icons.Default.CheckCircle, Forest)
                        StatChip("Flagged", state.students.count { it.hasFlaggedDepartment }.toString(),
                            Icons.Default.Flag, Color(0xFFFF8F00))
                        StatChip("Pending", state.students.count { !it.isFullyCleared && !it.hasFlaggedDepartment }.toString(),
                            Icons.Default.HourglassEmpty, Gold)
                    }
                    ClearanceProgressBar(
                        total   = state.students.size,
                        cleared = state.students.count { it.isFullyCleared },
                        flagged = state.students.count { it.hasFlaggedDepartment }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or admission no.", style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Maroon700) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon700,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Cleared", "Flagged").forEach { f ->
                    FilterChip(selected = selectedFilter == f, onClick = { selectedFilter = f },
                        label = { Text(f, style = MaterialTheme.typography.labelMedium) },
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Maroon700, selectedLabelColor = White),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedFilter == f,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            selectedBorderColor = Maroon700))
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                ) {
                    items(6) { StudentCardSkeleton() }
                }
                state.students.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(56.dp),
                            tint = Maroon700.copy(alpha = 0.3f))
                        Text("No students yet", style = MaterialTheme.typography.titleMedium)
                        Text("Tap \"Add Student\" to get started", style = MaterialTheme.typography.bodySmall, color = TextLight)
                    }
                }
                else -> {
                    val filtered = state.students
                        .filter { searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.admissionNumber.contains(searchQuery, true) }
                        .filter {
                            when (selectedFilter) {
                                "Cleared" -> it.isFullyCleared
                                "Flagged" -> it.hasFlaggedDepartment
                                "Pending" -> !it.isFullyCleared && !it.hasFlaggedDepartment
                                else      -> true
                            }
                        }
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
                        itemsIndexed(filtered, key = { _, it -> it.id }) { index, student ->
                            AnimatedListEntry(index = index) {
                                StudentCard(student = student,
                                    onClick   = { onStudentClick(student) },
                                    onMessage = { onMessageStudent(student) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Two concentric rings + a thin cross‑hair, echoing an official stamp. Purely decorative. */
@Composable
private fun SealWatermark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.height * 0.9f
        val center = androidx.compose.ui.geometry.Offset(size.width - r * 0.35f, size.height * 0.5f)
        val ringColor = White.copy(alpha = 0.06f)
        drawCircle(color = ringColor, radius = r * 0.5f, center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.02f))
        drawCircle(color = ringColor, radius = r * 0.38f, center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.015f))
        drawCircle(color = White.copy(alpha = 0.04f), radius = r * 0.22f, center = center)
    }
}

@Composable
private fun StatChip(label: String, value: String,
                     icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Gold) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(30.dp).background(color.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Text(value, style = MaterialTheme.typography.titleLarge, color = White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = White.copy(alpha = 0.7f))
    }
}

@Composable
private fun StudentCard(student: StudentModel, onClick: () -> Unit, onMessage: () -> Unit) {
    val overallStatus = when {
        student.hasFlaggedDepartment -> ClearanceStatus.FLAGGED
        student.isFullyCleared       -> ClearanceStatus.CLEARED
        else                         -> ClearanceStatus.PENDING
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp, pressedElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Slim status rail on the leading edge — reads the card's state before anything else.
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(overallStatus.toColor()))
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp)
                        .background(Brush.radialGradient(listOf(Maroon600, Maroon800)), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text(student.name.take(2).uppercase(), style = MaterialTheme.typography.titleSmall,
                            color = White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, style = MaterialTheme.typography.titleMedium)
                        Text(student.admissionNumber, style = MaterialTheme.typography.bodySmall, color = TextLight)
                        Text(student.email, style = MaterialTheme.typography.bodySmall, color = TextLight,
                            maxLines = 1)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = MaterialTheme.shapes.extraLarge,
                            color = overallStatus.toColor().copy(alpha = 0.12f)) {
                            Text(overallStatus.toLabel(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall, color = overallStatus.toColor())
                        }
                        IconButton(onClick = onMessage, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Message, "Message", tint = Maroon700, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Library" to student.libraryStatus, "Boarding" to student.boardingStatus,
                    "Sports" to student.sportsStatus, "Finance" to student.financeStatus).forEach { (label, status) ->
                    Box(modifier = Modifier.weight(1f).clip(MaterialTheme.shapes.extraSmall)
                        .background(status.toColor().copy(alpha = 0.14f)).padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = status.toColor())
                    }
                }
            }
        }
    }
}

/**
 * Live segmented bar showing cleared / pending / flagged as a proportion of
 * all students. Segments animate their width whenever the counts change —
 * this is the piece that makes the dashboard feel like a real analytics
 * product rather than a plain list.
 */
@Composable
private fun ClearanceProgressBar(total: Int, cleared: Int, flagged: Int) {
    val pending = (total - cleared - flagged).coerceAtLeast(0)
    val safeTotal = total.coerceAtLeast(1)
    val clearedFrac by animateFloatAsState(
        targetValue = cleared.toFloat() / safeTotal, animationSpec = tween(600), label = "clearedFrac")
    val flaggedFrac by animateFloatAsState(
        targetValue = flagged.toFloat() / safeTotal, animationSpec = tween(600), label = "flaggedFrac")
    val pendingFrac by animateFloatAsState(
        targetValue = pending.toFloat() / safeTotal, animationSpec = tween(600), label = "pendingFrac")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(White.copy(alpha = 0.15f))
        ) {
            if (total > 0) {
                Box(Modifier.weight(clearedFrac.coerceAtLeast(0.0001f)).fillMaxHeight().background(Forest))
                Box(Modifier.weight(pendingFrac.coerceAtLeast(0.0001f)).fillMaxHeight().background(Gold))
                Box(Modifier.weight(flaggedFrac.coerceAtLeast(0.0001f)).fillMaxHeight().background(Color(0xFFFF8F00)))
            }
        }
        Text(
            if (total == 0) "No students yet"
            else "${(clearedFrac * 100).toInt()}% cleared across $total student${if (total == 1) "" else "s"}",
            style = MaterialTheme.typography.bodySmall, color = White.copy(alpha = 0.8f)
        )
    }
}

/** Shimmering placeholder that mirrors StudentCard's layout while data loads. */
@Composable
private fun StudentCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(modifier = Modifier.size(50.dp), shape = CircleShape)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f).height(11.dp))
            }
        }
    }
}

/** Fades + slides a list item in the first time it appears in composition. */
@Composable
private fun AnimatedListEntry(index: Int, content: @Composable () -> Unit) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(280, delayMillis = (index * 35).coerceAtMost(300))) +
                slideInVertically(tween(280, delayMillis = (index * 35).coerceAtMost(300))) { it / 6 }
    ) {
        content()
    }
}