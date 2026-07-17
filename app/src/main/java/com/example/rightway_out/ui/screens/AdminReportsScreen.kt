package com.example.rightway_out.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.domain.model.ClearanceStatus
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.ui.components.toColor
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ClearanceViewModel

/**
 * Shared design tokens for this screen so spacing, alpha, and icon sizing stay
 * consistent across every card instead of being picked ad hoc per composable.
 * If other screens end up needing the same values, this is a good candidate
 * to hoist into ui/theme as an app-wide `Dimens`/`Alphas` object.
 */
private object ReportTokens {
    // Spacing scale
    val SpaceXS = 4.dp
    val SpaceSM = 8.dp
    val SpaceMD = 12.dp
    val SpaceLG = 16.dp
    val SpaceXL = 24.dp

    // Icon / avatar sizing scale
    val IconSM = 8.dp   // legend dots / status dots
    val IconMD = 20.dp  // icon inside the avatar chip
    val IconLG = 28.dp  // headline card icon
    val IconXL = 56.dp  // empty-state icon
    val AvatarMD = 38.dp

    // Bar / divider heights
    val BarHeight = 8.dp
    val CardElevation = 1.dp

    // Alpha scale — reused everywhere instead of one-off values per usage
    const val AlphaFaint = 0.06f      // subtle row background tint
    const val AlphaSubtle = 0.10f     // avatar chip background
    const val AlphaDisabled = 0.25f   // dividers / disabled icon tint
    const val AlphaMuted = 0.30f      // empty-state icon tint
    const val AlphaSecondaryText = 0.75f // secondary text on colored surfaces

    const val MinBarWeight = 0.0001f  // keeps zero-value bar segments from collapsing to 0
}

/**
 * A department is only ever as clear as its slowest sign-off, so this screen
 * reframes the same student list the Dashboard already loaded around the four
 * departments instead of the students — where is clearance actually stuck?
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onStudentClick: (StudentModel) -> Unit,
    viewModel: ClearanceViewModel = hiltViewModel()
) {
    val state by viewModel.adminState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadAllStudents() }

    val students = state.students
    val departments = remember {
        listOf(
            Department("Library", Icons.Default.MenuBook) { it.libraryStatus },
            Department("Boarding", Icons.Default.Hotel) { it.boardingStatus },
            Department("Sports", Icons.Default.SportsBasketball) { it.sportsStatus },
            Department("Finance", Icons.Default.Payments) { it.financeStatus },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Reports",
                            style = MaterialTheme.typography.titleLarge,
                            color = White
                        )
                        Text(
                            "Clearance by department",
                            style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = ReportTokens.AlphaSecondaryText)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            state.isLoading && students.isEmpty() -> LoadingState(padding)
            students.isEmpty() -> EmptyState(padding)
            else -> ReportContent(
                padding = padding,
                departments = departments,
                students = students,
                onStudentClick = onStudentClick
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Maroon700)
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ReportTokens.SpaceSM)
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(ReportTokens.IconXL),
                tint = Maroon700.copy(alpha = ReportTokens.AlphaMuted)
            )
            Text("Nothing to report yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add students to see department breakdowns",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
        }
    }
}

@Composable
private fun ReportContent(
    padding: PaddingValues,
    departments: List<Department>,
    students: List<StudentModel>,
    onStudentClick: (StudentModel) -> Unit
) {
    val mostStuck = departments.maxByOrNull { dept ->
        students.count { dept.status(it) != ClearanceStatus.CLEARED }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(ReportTokens.SpaceLG),
        verticalArrangement = Arrangement.spacedBy(ReportTokens.SpaceMD + ReportTokens.SpaceXS)
    ) {
        if (mostStuck != null) {
            item { HeadlineCard(department = mostStuck, students = students) }
        }
        items(departments) { dept ->
            DepartmentReportCard(
                department = dept,
                students = students,
                onStudentClick = onStudentClick
            )
        }
        item { Spacer(Modifier.height(ReportTokens.SpaceSM)) }
    }
}

private class Department(
    val label: String,
    val icon: ImageVector,
    val status: (StudentModel) -> ClearanceStatus
)

/** Calls out whichever department has the most unresolved cases — the one thing worth knowing first. */
@Composable
private fun HeadlineCard(department: Department, students: List<StudentModel>) {
    val stuck = students.count { department.status(it) != ClearanceStatus.CLEARED }
    if (stuck == 0) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.horizontalGradient(listOf(Maroon800, Maroon700)))
            .padding(ReportTokens.SpaceLG)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(ReportTokens.IconLG)
            )
            Spacer(Modifier.width(ReportTokens.SpaceMD))
            Column {
                Text(
                    "${department.label} is the current bottleneck",
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Text(
                    "$stuck student${if (stuck == 1) "" else "s"} still waiting on this department",
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = ReportTokens.AlphaSecondaryText)
                )
            }
        }
    }
}

@Composable
private fun DepartmentReportCard(
    department: Department,
    students: List<StudentModel>,
    onStudentClick: (StudentModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val total = students.size.coerceAtLeast(1)
    val cleared = students.count { department.status(it) == ClearanceStatus.CLEARED }
    val flagged = students.count { department.status(it) == ClearanceStatus.FLAGGED }
    val pending = students.size - cleared - flagged

    val clearedFrac by animateFloatAsState(cleared.toFloat() / total, tween(600), label = "cleared")
    val flaggedFrac by animateFloatAsState(flagged.toFloat() / total, tween(600), label = "flagged")
    val pendingFrac by animateFloatAsState(pending.toFloat() / total, tween(600), label = "pending")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(ReportTokens.CardElevation),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(ReportTokens.SpaceLG)) {
            DepartmentHeader(department, cleared, students.size, expanded)
            Spacer(Modifier.height(ReportTokens.SpaceMD))
            ProgressBar(clearedFrac, pendingFrac, flaggedFrac)
            Spacer(Modifier.height(ReportTokens.SpaceSM))
            Row(horizontalArrangement = Arrangement.spacedBy(ReportTokens.SpaceMD + ReportTokens.SpaceXS)) {
                LegendDot(Forest, "Cleared $cleared")
                LegendDot(Gold, "Pending $pending")
                LegendDot(Maroon700, "Flagged $flagged")
            }

            if (expanded) {
                UnresolvedList(department, students, onStudentClick)
            }
        }
    }
}

@Composable
private fun DepartmentHeader(
    department: Department,
    cleared: Int,
    totalStudents: Int,
    expanded: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(ReportTokens.AvatarMD)
                .background(Maroon700.copy(alpha = ReportTokens.AlphaSubtle), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                department.icon,
                contentDescription = null,
                tint = Maroon700,
                modifier = Modifier.size(ReportTokens.IconMD)
            )
        }
        Spacer(Modifier.width(ReportTokens.SpaceMD))
        Column(modifier = Modifier.weight(1f)) {
            Text(department.label, style = MaterialTheme.typography.titleMedium)
            Text(
                "$cleared of $totalStudents cleared",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
        }
        Icon(
            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = TextLight
        )
    }
}

@Composable
private fun ProgressBar(clearedFrac: Float, pendingFrac: Float, flaggedFrac: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ReportTokens.BarHeight)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.weight(clearedFrac.coerceAtLeast(ReportTokens.MinBarWeight)).fillMaxHeight().background(Forest))
        Box(Modifier.weight(pendingFrac.coerceAtLeast(ReportTokens.MinBarWeight)).fillMaxHeight().background(Gold))
        Box(Modifier.weight(flaggedFrac.coerceAtLeast(ReportTokens.MinBarWeight)).fillMaxHeight().background(Maroon700))
    }
}

@Composable
private fun UnresolvedList(
    department: Department,
    students: List<StudentModel>,
    onStudentClick: (StudentModel) -> Unit
) {
    val unresolved = students
        .filter { department.status(it) != ClearanceStatus.CLEARED }
        .sortedByDescending { department.status(it) == ClearanceStatus.FLAGGED }

    Spacer(Modifier.height(ReportTokens.SpaceMD))
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = ReportTokens.AlphaDisabled))
    Spacer(Modifier.height(ReportTokens.SpaceSM))

    if (unresolved.isEmpty()) {
        Text("Everyone is cleared here.", style = MaterialTheme.typography.bodySmall, color = TextLight)
        return
    }

    unresolved.take(8).forEach { student ->
        val status = department.status(student)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(status.toColor().copy(alpha = ReportTokens.AlphaFaint))
                .padding(vertical = ReportTokens.SpaceSM, horizontal = ReportTokens.SpaceSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(ReportTokens.IconSM).background(status.toColor(), CircleShape))
            Spacer(Modifier.width(ReportTokens.SpaceMD - ReportTokens.SpaceXS))
            Text(
                student.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onStudentClick(student) }) {
                Text("View", style = MaterialTheme.typography.labelMedium, color = Maroon700)
            }
        }
    }
    if (unresolved.size > 8) {
        Text(
            "+ ${unresolved.size - 8} more",
            style = MaterialTheme.typography.bodySmall,
            color = TextLight,
            modifier = Modifier.padding(start = ReportTokens.SpaceSM, top = ReportTokens.SpaceXS)
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(ReportTokens.IconSM).background(color, CircleShape))
        Spacer(Modifier.width(ReportTokens.SpaceSM - ReportTokens.SpaceXS))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMid)
    }
}
