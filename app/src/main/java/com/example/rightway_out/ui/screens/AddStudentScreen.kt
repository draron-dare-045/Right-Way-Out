package com.example.rightway_out.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Holds the admin password in memory for the session only (never saved to disk)
object AdminSession {
    var password: String = ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var name            by remember { mutableStateOf("") }
    var admissionNumber by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var successMessage  by remember { mutableStateOf<String?>(null) }

    // If admin password not yet stored, ask for it once
    var adminPassword       by remember { mutableStateOf(AdminSession.password) }
    var showAdminPassDialog by remember { mutableStateOf(AdminSession.password.isEmpty()) }
    var adminPassInput      by remember { mutableStateOf("") }
    var adminPassVisible    by remember { mutableStateOf(false) }

    // Dialog to capture admin password once per session
    if (showAdminPassDialog) {
        AlertDialog(
            onDismissRequest = { onBack() },
            icon = { Icon(Icons.Default.AdminPanelSettings, null) },
            title = { Text("Confirm Your Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("To create student accounts, please enter your admin password once.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = adminPassInput,
                        onValueChange = { adminPassInput = it },
                        label = { Text("Your Admin Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { adminPassVisible = !adminPassVisible }) {
                                Icon(if (adminPassVisible) Icons.Default.VisibilityOff
                                     else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (adminPassVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (adminPassInput.isNotBlank()) {
                        AdminSession.password = adminPassInput
                        adminPassword = adminPassInput
                        showAdminPassDialog = false
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { onBack() }) { Text("Cancel") }
            }
        )
    }

    fun createStudent() {
        if (name.isBlank())            { errorMessage = "Name is required."; return }
        if (admissionNumber.isBlank()) { errorMessage = "Admission number is required."; return }
        if (email.isBlank())           { errorMessage = "Email is required."; return }
        if (password.length < 6)       { errorMessage = "Password must be at least 6 characters."; return }

        isLoading = true
        errorMessage = null
        successMessage = null

        scope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val adminEmail = auth.currentUser?.email
                    ?: throw Exception("Could not identify admin account.")

                // Step 1 — create the student Auth account
                val result = auth.createUserWithEmailAndPassword(
                    email.trim(), password.trim()
                ).await()

                val newUid = result.user?.uid
                    ?: throw Exception("Failed to get new user ID.")

                // Step 2 — write Firestore document
                val studentData = hashMapOf(
                    "name"            to name.trim(),
                    "admissionNumber" to admissionNumber.trim(),
                    "email"           to email.trim(),
                    "role"            to "STUDENT",
                    "libraryStatus"   to "PENDING",
                    "boardingStatus"  to "PENDING",
                    "sportsStatus"    to "PENDING",
                    "financeStatus"   to "PENDING",
                    "libraryComment"  to "",
                    "boardingComment" to "",
                    "sportsComment"   to "",
                    "financeComment"  to ""
                )
                FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(newUid)
                    .set(studentData)
                    .await()

                // Step 3 — sign admin back in immediately
                auth.signInWithEmailAndPassword(adminEmail, adminPassword).await()

                successMessage = "✅ ${name.trim()} added successfully!"
                isLoading = false

                // Clear form for next student
                name = ""
                admissionNumber = ""
                email = ""
                password = ""

            } catch (e: Exception) {
                isLoading = false

                // If re-login failed, clear stored password so dialog shows again
                if (e.message?.contains("password is invalid") == true ||
                    e.message?.contains("credential is incorrect") == true) {
                    AdminSession.password = ""
                    adminPassword = ""
                    showAdminPassDialog = true
                    errorMessage = "Admin password was wrong. Please re-enter it."
                } else {
                    errorMessage = when {
                        e.message?.contains("already in use") == true ->
                            "This email is already registered."
                        e.message?.contains("badly formatted") == true ->
                            "Please enter a valid email address."
                        else -> e.localizedMessage ?: "Failed to create student."
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Student", fontWeight = FontWeight.Bold)
                        Text("Create a new student account", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("The student will use their email and password to log in to the app.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Full Name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = admissionNumber, onValueChange = { admissionNumber = it },
                label = { Text("Admission Number") }, leadingIcon = { Icon(Icons.Default.Badge, null) },
                placeholder = { Text("e.g. SCH/2024/001") },
                singleLine = true, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("School Email") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Temporary Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff
                             else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = { Text("Minimum 6 characters") },
                singleLine = true, modifier = Modifier.fillMaxWidth())

            errorMessage?.let {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            successMessage?.let {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { createStudent() },
                enabled = !isLoading && !showAdminPassDialog,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Creating account...", fontSize = 15.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PersonAdd, null)
                        Text("Add Student", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (successMessage != null) {
                OutlinedButton(onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ArrowBack, null)
                        Text("Back to Admin Panel", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
