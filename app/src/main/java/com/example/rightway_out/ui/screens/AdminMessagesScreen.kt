package com.example.rightway_out.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class ConversationPreview(
    val studentId: String = "",
    val studentName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessagesScreen(
    onBack: () -> Unit,
    onOpenChat: (studentId: String, studentName: String) -> Unit
) {
    val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db       = FirebaseDatabase.getInstance()
    val fs       = FirebaseFirestore.getInstance()

    var conversations by remember { mutableStateOf<List<ConversationPreview>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var searchQuery   by remember { mutableStateOf("") }

    // Load all students then check their chat nodes for last message
    LaunchedEffect(Unit) {
        fs.collection("students").get().addOnSuccessListener { snap ->
            val studentMap = snap.documents.associate { doc ->
                doc.id to (doc.getString("name") ?: "Unknown")
            }
            val convList = mutableListOf<ConversationPreview>()
            var processed = 0
            if (studentMap.isEmpty()) { isLoading = false; return@addOnSuccessListener }

            studentMap.forEach { (sid, name) ->
                if (sid == adminUid) { processed++; if (processed == studentMap.size) { conversations = convList.sortedByDescending { it.timestamp }; isLoading = false }; return@forEach }
                val chatId  = sid
                val chatRef = db.getReference("chats/$chatId")
                chatRef.orderByChild("timestamp").limitToLast(1)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snap: DataSnapshot) {
                            if (snap.exists()) {
                                val child = snap.children.first()
                                convList.add(ConversationPreview(
                                    studentId   = sid,
                                    studentName = name,
                                    lastMessage = child.child("text").getValue(String::class.java) ?: "",
                                    timestamp   = child.child("timestamp").getValue(Long::class.java) ?: 0L
                                ))
                            }
                            processed++
                            if (processed == studentMap.size) {
                                conversations = convList.sortedByDescending { it.timestamp }
                                isLoading = false
                            }
                        }
                        override fun onCancelled(e: DatabaseError) {
                            processed++
                            if (processed == studentMap.size) { conversations = convList.sortedByDescending { it.timestamp }; isLoading = false }
                        }
                    })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Messages", fontWeight = FontWeight.Bold, color = White)
                        Text("Student conversations", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon700)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Maroon700)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Maroon700)
                }
            } else if (conversations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, null,
                            modifier = Modifier.size(56.dp), tint = Maroon700.copy(alpha = 0.3f))
                        Text("No conversations yet", color = TextMid)
                        Text("Message a student from their profile", fontSize = 13.sp, color = TextLight)
                    }
                }
            } else {
                val filtered = conversations.filter {
                    searchQuery.isBlank() || it.studentName.contains(searchQuery, true)
                }
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(filtered, key = { it.studentId }) { conv ->
                        ConversationRow(conv = conv,
                            onClick = { onOpenChat(conv.studentId, conv.studentName) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conv: ConversationPreview, onClick: () -> Unit) {
    val timeStr = if (conv.timestamp > 0L) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(conv.timestamp))
    } else ""

    val unreadCount = rememberChatUnreadCount(studentId = conv.studentId, viewerIsAdmin = true)
    val hasUnread = unreadCount > 0

    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }
        .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {

        Box(modifier = Modifier.size(50.dp)
            .background(Maroon700, CircleShape),
            contentAlignment = Alignment.Center) {
            Text(conv.studentName.take(2).uppercase(), fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = White)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(conv.studentName,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 15.sp)
            Text(conv.lastMessage, fontSize = 13.sp,
                color = if (hasUnread) TextDark else TextLight,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(timeStr, fontSize = 11.sp,
                color = if (hasUnread) Forest else TextLight,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal)
            if (hasUnread) {
                Box(
                    modifier = Modifier.background(Forest, CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (unreadCount > 99) "99+" else unreadCount.toString(),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White
                    )
                }
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}