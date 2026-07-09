package com.example.rightway_out.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AdminSession { var password: String = "" }

data class StudentFormData(
    val name: String = "", val admissionNumber: String = "",
    val email: String = "", val password: String = "",
    val classYear: String = "", val stream: String = "",
    val dormitory: String = "", val parentPhone: String = "",
    val parentName: String = "", val county: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var form by remember { mutableStateOf(StudentFormData()) }
    var passwordVisible  by remember { mutableStateOf(false) }
    var isLoading        by remember { mutableStateOf(false) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }
    var successMessage   by remember { mutableStateOf<String?>(null) }
    var successCount     by remember { mutableStateOf(0) }
    var adminPassword    by remember { mutableStateOf(AdminSession.password) }
    var showAdminDialog  by remember { mutableStateOf(AdminSession.password.isEmpty()) }
    var adminPassInput   by remember { mutableStateOf("") }
    var adminPassVisible by remember { mutableStateOf(false) }
    var showImportInfo   by remember { mutableStateOf(false) }
    var isImporting      by remember { mutableStateOf(false) }
    var importStatus     by remember { mutableStateOf("") }

    // Stream options per form, dormitory list
    val classYears  = listOf("Form 1", "Form 2", "Form 3", "Form 4")
    val streams     = listOf("A", "B", "C", "D", "E")
    val dormitories = listOf("Chesoi", "Kapcherop", "Moiben", "Kipkaren", "Nzoia", "Sosiani", "Kerio")
    var classExpanded   by remember { mutableStateOf(false) }
    var streamExpanded  by remember { mutableStateOf(false) }
    var dormExpanded    by remember { mutableStateOf(false) }

    // CSV file picker
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isImporting = true
            importStatus = "Reading file..."
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val lines = inputStream?.bufferedReader()?.readLines() ?: emptyList()
                    inputStream?.close()

                    // Expected CSV format:
                    // name,admissionNumber,email,password,classYear,stream,dormitory,parentPhone,parentName,county
                    val dataLines = if (lines.firstOrNull()?.startsWith("name", true) == true)
                        lines.drop(1) else lines

                    val auth = FirebaseAuth.getInstance()
                    val adminEmail = auth.currentUser?.email ?: throw Exception("Admin not logged in")
                    var imported = 0; var failed = 0

                    dataLines.forEachIndexed { index, line ->
                        if (line.isBlank()) return@forEachIndexed
                        importStatus = "Importing ${index + 1}/${dataLines.size}..."
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size < 4) { failed++; return@forEachIndexed }
                        try {
                            val result = auth.createUserWithEmailAndPassword(
                                parts[2], parts[3]).await()
                            val uid = result.user?.uid ?: throw Exception("No UID")
                            val data = hashMapOf(
                                "name" to parts[0], "admissionNumber" to parts[1],
                                "email" to parts[2], "role" to "STUDENT",
                                "classYear" to (parts.getOrNull(4) ?: ""),
                                "stream" to (parts.getOrNull(5) ?: ""),
                                "dormitory" to (parts.getOrNull(6) ?: ""),
                                "parentPhone" to (parts.getOrNull(7) ?: ""),
                                "parentName" to (parts.getOrNull(8) ?: ""),
                                "county" to (parts.getOrNull(9) ?: ""),
                                "libraryStatus" to "PENDING", "boardingStatus" to "PENDING",
                                "sportsStatus" to "PENDING", "financeStatus" to "PENDING",
                                "libraryComment" to "", "boardingComment" to "",
                                "sportsComment" to "", "financeComment" to ""
                            )
                            FirebaseFirestore.getInstance()
                                .collection("students").document(uid).set(data).await()
                            imported++
                            auth.signInWithEmailAndPassword(adminEmail, adminPassword).await()
                        } catch (e: Exception) { failed++; auth.signInWithEmailAndPassword(adminEmail, adminPassword).await() }
                    }
                    importStatus = "Done! $imported imported, $failed failed."
                    isImporting = false
                } catch (e: Exception) {
                    importStatus = "Error: ${e.localizedMessage}"
                    isImporting = false
                }
            }
        }
    }

    fun createStudent() {
        if (form.name.isBlank())           { errorMessage = "Name is required."; return }
        if (form.admissionNumber.isBlank()){ errorMessage = "Admission number is required."; return }
        if (form.email.isBlank())          { errorMessage = "Email is required."; return }
        if (form.password.length < 6)      { errorMessage = "Password must be at least 6 characters."; return }

        isLoading = true; errorMessage = null; successMessage = null
        scope.launch {
            try {
                val auth       = FirebaseAuth.getInstance()
                val adminEmail = auth.currentUser?.email ?: throw Exception("Not logged in")
                val result     = auth.createUserWithEmailAndPassword(form.email.trim(), form.password.trim()).await()
                val uid        = result.user?.uid ?: throw Exception("No UID returned")
                val data = hashMapOf(
                    "name" to form.name.trim(), "admissionNumber" to form.admissionNumber.trim(),
                    "email" to form.email.trim(), "role" to "STUDENT",
                    "classYear" to form.classYear, "stream" to form.stream,
                    "dormitory" to form.dormitory, "parentPhone" to form.parentPhone.trim(),
                    "parentName" to form.parentName.trim(), "county" to form.county.trim(),
                    "libraryStatus" to "PENDING", "boardingStatus" to "PENDING",
                    "sportsStatus" to "PENDING", "financeStatus" to "PENDING",
                    "libraryComment" to "", "boardingComment" to "",
                    "sportsComment" to "", "financeComment" to "",
                    "profilePicUrl" to ""
                )
                FirebaseFirestore.getInstance().collection("students").document(uid).set(data).await()
                auth.signInWithEmailAndPassword(adminEmail, adminPassword).await()
                successCount++
                successMessage = "✅ ${form.name.trim()} added! (Total: $successCount)"
                isLoading = false
                form = StudentFormData()
            } catch (e: Exception) {
                isLoading = false
                if (e.message?.contains("password is invalid") == true) {
                    AdminSession.password = ""; adminPassword = ""; showAdminDialog = true
                }
                errorMessage = when {
                    e.message?.contains("already in use") == true -> "This email is already registered."
                    e.message?.contains("badly formatted") == true -> "Invalid email format."
                    else -> e.localizedMessage ?: "Failed to create student."
                }
            }
        }
    }

    // Admin password dialog
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { onBack() },
            icon  = { Icon(Icons.Default.AdminPanelSettings, null, tint = Maroon700) },
            title = { Text("Confirm Admin Password") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your password once to enable student account creation.",
                        fontSize = 13.sp, color = TextMid)
                    OutlinedTextField(
                        value = adminPassInput, onValueChange = { adminPassInput = it },
                        label = { Text("Your Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { adminPassVisible = !adminPassVisible }) {
                                Icon(if (adminPassVisible) Icons.Default.VisibilityOff
                                     else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (adminPassVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (adminPassInput.isNotBlank()) {
                        AdminSession.password = adminPassInput
                        adminPassword = adminPassInput
                        showAdminDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Maroon700)) {
                    Text("Confirm")
                }
            },
            dismissButton = { TextButton(onClick = onBack) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Student", fontWeight = FontWeight.Bold, color = White)
                        Text("Create a new student account", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = White) }
                },
                actions = {
                    IconButton(onClick = { showImportInfo = !showImportInfo }) {
                        Icon(Icons.Default.UploadFile, "Import CSV", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // CSV import card
            if (showImportInfo) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Bulk Import via CSV", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text("CSV columns (in order):\nname, admissionNumber, email, password, classYear, stream, dormitory, parentPhone, parentName, county",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        if (importStatus.isNotBlank()) {
                            Text(importStatus, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Button(onClick = { csvLauncher.launch("text/*") },
                            enabled = !isImporting,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon700)) {
                            if (isImporting) {
                                CircularProgressIndicator(color = White,
                                    modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Importing...", color = White)
                            } else {
                                Icon(Icons.Default.FileUpload, null, tint = White)
                                Spacer(Modifier.width(8.dp))
                                Text("Choose CSV File", color = White)
                            }
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Maroon700, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Student uses email + password to log in.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // ── SECTION: Personal Info ─────────────────────────
            SectionHeader("Personal Information")

            FormField("Full Name", form.name, Icons.Default.Person) { form = form.copy(name = it) }
            FormField("Admission Number", form.admissionNumber, Icons.Default.Badge,
                placeholder = "e.g. KHS/2024/001") { form = form.copy(admissionNumber = it) }
            FormField("County / Home Area", form.county, Icons.Default.LocationOn) { form = form.copy(county = it) }
            FormField("Parent/Guardian Name", form.parentName, Icons.Default.FamilyRestroom) { form = form.copy(parentName = it) }
            FormField("Parent Phone", form.parentPhone, Icons.Default.Phone,
                keyboardType = KeyboardType.Phone) { form = form.copy(parentPhone = it) }

            // ── SECTION: Academic Info ─────────────────────────
            SectionHeader("Academic Information")

            // Class Year dropdown
            ExposedDropdownMenuBox(expanded = classExpanded, onExpandedChange = { classExpanded = !classExpanded }) {
                OutlinedTextField(
                    value = form.classYear.ifBlank { "Select Class" }, onValueChange = {}, readOnly = true,
                    label = { Text("Class / Form") }, leadingIcon = { Icon(Icons.Default.School, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(classExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700, focusedLabelColor = Maroon700))
                ExposedDropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                    classYears.forEach { cy ->
                        DropdownMenuItem(text = { Text(cy) }, onClick = { form = form.copy(classYear = cy); classExpanded = false })
                    }
                }
            }

            // Stream dropdown
            ExposedDropdownMenuBox(expanded = streamExpanded, onExpandedChange = { streamExpanded = !streamExpanded }) {
                OutlinedTextField(
                    value = form.stream.ifBlank { "Select Stream" }, onValueChange = {}, readOnly = true,
                    label = { Text("Stream") }, leadingIcon = { Icon(Icons.Default.Groups, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(streamExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700, focusedLabelColor = Maroon700))
                ExposedDropdownMenu(expanded = streamExpanded, onDismissRequest = { streamExpanded = false }) {
                    streams.forEach { s ->
                        DropdownMenuItem(text = { Text("Stream $s") }, onClick = { form = form.copy(stream = s); streamExpanded = false })
                    }
                }
            }

            // Dormitory dropdown
            ExposedDropdownMenuBox(expanded = dormExpanded, onExpandedChange = { dormExpanded = !dormExpanded }) {
                OutlinedTextField(
                    value = form.dormitory.ifBlank { "Select Dormitory" }, onValueChange = {}, readOnly = true,
                    label = { Text("Dormitory") }, leadingIcon = { Icon(Icons.Default.Hotel, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dormExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700, focusedLabelColor = Maroon700))
                ExposedDropdownMenu(expanded = dormExpanded, onDismissRequest = { dormExpanded = false }) {
                    dormitories.forEach { d ->
                        DropdownMenuItem(text = { Text(d) }, onClick = { form = form.copy(dormitory = d); dormExpanded = false })
                    }
                }
            }

            // ── SECTION: Account Credentials ──────────────────
            SectionHeader("Login Credentials")

            FormField("School Email", form.email, Icons.Default.Email,
                keyboardType = KeyboardType.Email) { form = form.copy(email = it) }

            OutlinedTextField(
                value = form.password, onValueChange = { form = form.copy(password = it) },
                label = { Text("Temporary Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Maroon700) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = { Text("Min. 6 characters") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700, focusedLabelColor = Maroon700)
            )

            // Messages
            errorMessage?.let {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
            successMessage?.let {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Forest, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = Forest, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Button(onClick = { createStudent() }, enabled = !isLoading && !showAdminDialog,
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Maroon700)) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Creating...", color = White)
                } else {
                    Icon(Icons.Default.PersonAdd, null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Student", color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (successMessage != null) {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Back to Admin Panel")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Divider(modifier = Modifier.width(20.dp), color = Maroon700, thickness = 2.dp)
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            color = Maroon700, letterSpacing = 1.sp)
        Divider(modifier = Modifier.weight(1f), color = Maroon700.copy(alpha = 0.2f))
    }
}

@Composable
private fun FormField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "", keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Maroon700) },
        placeholder = if (placeholder.isNotBlank()) {{ Text(placeholder) }} else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700, focusedLabelColor = Maroon700)
    )
}
