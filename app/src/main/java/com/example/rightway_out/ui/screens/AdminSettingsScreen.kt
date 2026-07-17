package com.example.rightway_out.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val isDark by themeViewModel.isDarkMode.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Settings", style = MaterialTheme.typography.titleLarge, color = White)
                        Text("Account & preferences", style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = 0.75f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account card
            Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                .background(Brush.horizontalGradient(listOf(Maroon800, Maroon700)))
                .padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(54.dp).background(Gold, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Maroon900, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(user?.email ?: "Admin", style = MaterialTheme.typography.titleMedium, color = White)
                        Text("Kapsabet High School · Administrator",
                            style = MaterialTheme.typography.bodySmall, color = White.copy(alpha = 0.75f))
                    }
                }
            }

            SettingsSection(title = "Appearance") {
                SettingsRow(
                    icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark mode",
                    subtitle = if (isDark) "On" else "Off"
                ) {
                    Switch(
                        checked = isDark,
                        onCheckedChange = { themeViewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = Maroon700)
                    )
                }
            }

            SettingsSection(title = "About") {
                SettingsRow(icon = Icons.Default.School, title = "School", subtitle = "Kapsabet High School")
                SettingsRow(icon = Icons.Default.Info, title = "App version", subtitle = "1.0.0")
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { showSignOutConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon700)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign out", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign out?", style = MaterialTheme.typography.titleMedium) },
            text = { Text("You'll need to log back in to access the admin panel.",
                style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutConfirm = false
                    onLogout()
                }) { Text("Sign out", color = Maroon700) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextLight,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Maroon700, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextLight)
        }
        trailing?.invoke()
    }
}