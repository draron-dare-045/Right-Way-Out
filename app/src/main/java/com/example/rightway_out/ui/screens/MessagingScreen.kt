package com.example.rightway_out.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isAdmin: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MessagingScreen(
    studentId: String,
    studentName: String,
    onBack: () -> Unit
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isAdmin = currentUid != studentId
    val db = FirebaseDatabase.getInstance()
    // One conversation thread per student — keyed by studentId alone so the
    // student and any admin viewing that student's chat land in the same room.
    val chatId = studentId
    val chatRef = db.getReference("chats/$chatId")

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val isInputEnabled = !isSending
    val canSend = inputText.trim().isNotEmpty() && !isSending && senderName.isNotEmpty()

    // Fetch sender name
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            isLoading = true
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("students").document(currentUid).get().await()
                senderName = doc.getString("name") ?: if (isAdmin) "Admin" else "Student"
            } catch (e: Exception) {
                senderName = if (isAdmin) "Admin" else "Student"
                errorMessage = "Failed to load user info"
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-focus input when ready
    LaunchedEffect(senderName) {
        if (senderName.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }

    // Clear this chat's unread badge for the current viewer whenever they're
    // actively looking at it — mirrors WhatsApp marking a thread read on open.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            markChatAsRead(studentId, viewerIsAdmin = isAdmin)
        }
    }

    // Realtime messages listener
    DisposableEffect(chatRef) {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snap: DataSnapshot, prev: String?) {
                val msg = snap.toChatMessage()
                if (!messages.any { it.id == msg.id }) {
                    messages = (messages + msg).sortedBy { it.timestamp }
                    scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                }
            }

            override fun onChildChanged(snap: DataSnapshot, prev: String?) {
                val updated = snap.toChatMessage()
                messages = messages.map { if (it.id == updated.id) updated else it }
            }

            override fun onChildRemoved(snap: DataSnapshot) {
                messages = messages.filter { it.id != snap.key }
            }

            override fun onChildMoved(snap: DataSnapshot, prev: String?) {}
            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Database error: ${error.message}"
            }
        }

        chatRef.addChildEventListener(listener)
        onDispose { chatRef.removeEventListener(listener) }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isSending || senderName.isEmpty()) return

        isSending = true
        inputText = ""

        val msgRef = chatRef.push()
        msgRef.setValue(
            mapOf(
                "senderId" to currentUid,
                "senderName" to senderName,
                "text" to text,
                "timestamp" to ServerValue.TIMESTAMP,
                "isAdmin" to isAdmin
            )
        ).addOnCompleteListener { task ->
            isSending = false
            if (!task.isSuccessful) {
                errorMessage = "Failed to send: ${task.exception?.localizedMessage}"
                inputText = text // restore
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Gold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                studentName.take(2).uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        Column {
                            Text(studentName, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = White)
                            Text(
                                if (isAdmin) "Student Chat" else "Admin Support",
                                fontSize = 12.sp,
                                color = White.copy(alpha = 0.75f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Error banner
            errorMessage?.let { msg ->
                Surface(color = Color(0xFFFFEBEE), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFC62828))
                        Spacer(Modifier.width(8.dp))
                        Text(msg, color = Color(0xFFC62828), modifier = Modifier.weight(1f))
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFC62828))
                        }
                    }
                }
            }

            // Messages area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(64.dp), tint = Maroon700.copy(alpha = 0.25f))
                                Spacer(Modifier.height(12.dp))
                                Text("No messages yet", style = MaterialTheme.typography.titleMedium, color = TextLight)
                                Text("Send the first message below 👇", fontSize = 14.sp, color = TextLight)
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == currentUid
                    val visibleState = remember(msg.id) { MutableTransitionState(false).apply { targetState = true } }

                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 4 }
                    ) {
                        ChatBubble(message = msg, isMine = isMine)
                    }
                }
            }

            // Input Bar
            Surface(
                shadowElevation = 8.dp,
                color = White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        enabled = isInputEnabled,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon700,
                            unfocusedBorderColor = CreamDark
                        )
                    )

                    FloatingActionButton(
                        onClick = { sendMessage() },
                        containerColor = if (canSend) Maroon700 else Maroon700.copy(alpha = 0.6f),
                        contentColor = if (canSend) White else White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Extension helper
private fun DataSnapshot.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = key ?: "",
        senderId = child("senderId").getValue(String::class.java) ?: "",
        senderName = child("senderName").getValue(String::class.java) ?: "",
        text = child("text").getValue(String::class.java) ?: "",
        timestamp = child("timestamp").getValue(Long::class.java) ?: 0L,
        isAdmin = child("isAdmin").getValue(Boolean::class.java) ?: false
    )
}

@Composable
private fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Maroon700, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.senderName.take(1).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (!isMine) {
                Text(
                    message.senderName,
                    fontSize = 12.sp,
                    color = TextLight,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        if (isMine)
                            Brush.horizontalGradient(listOf(Maroon700, Maroon600))
                        else
                            Brush.horizontalGradient(listOf(White, Color(0xFFF5F5F5))),
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMine) 18.dp else 6.dp,
                            bottomEnd = if (isMine) 6.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Text(
                    message.text,
                    fontSize = 15.sp,
                    color = if (isMine) White else TextDark,
                    lineHeight = 20.sp
                )
            }
        }
    }
}