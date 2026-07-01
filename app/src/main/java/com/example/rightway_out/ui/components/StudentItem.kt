package com.example.rightway_out.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.domain.model.ClearanceStatus
import com.example.rightway_out.domain.model.StudentModel

@Composable
fun StudentItem(student: StudentModel, onEditClick: (StudentModel) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(46.dp), shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text(student.name.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(student.admissionNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val overallStatus = when {
                student.hasFlaggedDepartment -> ClearanceStatus.FLAGGED
                student.isFullyCleared       -> ClearanceStatus.CLEARED
                else                         -> ClearanceStatus.PENDING
            }
            ClearanceBadge(overallStatus)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onEditClick(student) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ${student.name}",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
