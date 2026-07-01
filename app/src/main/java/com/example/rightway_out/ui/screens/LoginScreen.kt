package com.example.rightway_out.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rightway_out.ui.theme.*
import com.example.rightway_out.ui.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    onLoginSuccess: (uid: String, isAdmin: Boolean) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isCheckingRole by remember { mutableStateOf(false) }

    fun handleLoginSuccess(uid: String) {
        isCheckingRole = true
        scope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("students").document(uid).get().await()
                val role = doc.getString("role") ?: "STUDENT"
                onLoginSuccess(uid, role == "ADMIN")
            } catch (e: Exception) {
                onLoginSuccess(uid, false)
            } finally {
                isCheckingRole = false
            }
        }
    }

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            val uid = viewModel.currentUser?.uid ?: return@LaunchedEffect
            handleLoginSuccess(uid)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaroonDark, MaroonPrimary, MaroonLight))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // School branding header
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier.size(80.dp)
                        .background(GoldAccent, RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("KHS", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = White)
                }
                Text("KAPSABET HIGH SCHOOL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                    color = White, letterSpacing = 1.5.sp)
                Text("RightWay Out — Clearance System", fontSize = 12.sp,
                    color = White.copy(alpha = 0.75f))
                Divider(modifier = Modifier.width(60.dp), color = GoldAccent, thickness = 2.dp)
            }

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Sign In", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = MaroonPrimary)
                    Text("Enter your school credentials to continue",
                        fontSize = 13.sp, color = TextMedium)

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("School Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = MaroonPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaroonPrimary,
                            focusedLabelColor = MaroonPrimary
                        )
                    )

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaroonPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff
                                     else Icons.Default.Visibility, null, tint = MaroonPrimary)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaroonPrimary,
                            focusedLabelColor = MaroonPrimary
                        )
                    )

                    authState.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.login(email, password) },
                        enabled = !authState.isLoading && !isCheckingRole,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                    ) {
                        if (authState.isLoading || isCheckingRole) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = White,
                                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text(if (isCheckingRole) "Checking role..." else "Signing in...",
                                    color = White)
                            }
                        } else {
                            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                }
            }

            Text("Kapsabet High School © 2025", fontSize = 11.sp,
                color = White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        }
    }
}
