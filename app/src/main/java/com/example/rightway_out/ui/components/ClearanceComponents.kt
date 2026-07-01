package com.example.rightway_out.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.domain.model.ClearanceStatus

val StatusPending = Color(0xFFFFA726)
val StatusCleared = Color(0xFF43A047)
val StatusFlagged = Color(0xFFE53935)

fun ClearanceStatus.toColor() = when (this) {
    ClearanceStatus.CLEARED -> StatusCleared
    ClearanceStatus.FLAGGED -> StatusFlagged
    ClearanceStatus.PENDING -> StatusPending
}

fun ClearanceStatus.toLabel() = when (this) {
    ClearanceStatus.CLEARED -> "Cleared"
    ClearanceStatus.FLAGGED -> "Flagged"
    ClearanceStatus.PENDING -> "Pending"
}

fun ClearanceStatus.toIcon(): ImageVector = when (this) {
    ClearanceStatus.CLEARED -> Icons.Default.CheckCircle
    ClearanceStatus.FLAGGED -> Icons.Default.Cancel
    ClearanceStatus.PENDING -> Icons.Default.HourglassEmpty
}

@Composable
fun ClearanceBadge(status: ClearanceStatus, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(status.toColor().copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(status.toIcon(), contentDescription = status.toLabel(),
                tint = status.toColor(), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(status.toLabel(), color = status.toColor(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DepartmentCard(department: String, icon: ImageVector, status: ClearanceStatus, comment: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(status.toColor().copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = department, tint = status.toColor(), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(department, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (comment.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            ClearanceBadge(status)
        }
    }
}
