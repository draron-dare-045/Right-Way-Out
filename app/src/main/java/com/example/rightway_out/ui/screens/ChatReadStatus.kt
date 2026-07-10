package com.example.rightway_out.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Read-status tracking for chat threads, RTDB-backed.
 *
 * Each student has exactly one thread at chats/{studentId}. Alongside it we keep
 * a small "how far has each side read" marker at chat_reads/{studentId}:
 *
 *   chat_reads/{studentId}/student -> Long (server timestamp, student's last-read point)
 *   chat_reads/{studentId}/admin   -> Long (server timestamp, admin's last-read point)
 *
 * A message counts as unread for a viewer when it was sent by the *other* party
 * and its timestamp is after that viewer's last-read marker — the same logic
 * WhatsApp uses per-chat.
 */

/** Call when a chat screen is actively open/visible, to clear its unread badge. */
fun markChatAsRead(studentId: String, viewerIsAdmin: Boolean) {
    if (studentId.isEmpty()) return
    val role = if (viewerIsAdmin) "admin" else "student"
    FirebaseDatabase.getInstance()
        .getReference("chat_reads/$studentId/$role")
        .setValue(ServerValue.TIMESTAMP)
}

private fun DataSnapshot.toChatMessages(): List<ChatMessage> =
    children.mapNotNull { child ->
        ChatMessage(
            id = child.key ?: "",
            senderId = child.child("senderId").getValue(String::class.java) ?: "",
            senderName = child.child("senderName").getValue(String::class.java) ?: "",
            text = child.child("text").getValue(String::class.java) ?: "",
            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
            isAdmin = child.child("isAdmin").getValue(Boolean::class.java) ?: false
        )
    }

/**
 * Live unread count for a single student's chat, from the given viewer's perspective.
 * Recomposes automatically as new messages arrive or the read marker moves.
 */
@Composable
fun rememberChatUnreadCount(studentId: String, viewerIsAdmin: Boolean): Int {
    var unreadCount by mutableStateOf(0)

    DisposableEffect(studentId, viewerIsAdmin) {
        if (studentId.isEmpty()) {
            return@DisposableEffect onDispose {}
        }

        val db = FirebaseDatabase.getInstance()
        val messagesRef = db.getReference("chats/$studentId")
        val readRef = db.getReference("chat_reads/$studentId/${if (viewerIsAdmin) "admin" else "student"}")

        var lastRead = 0L
        var messages: List<ChatMessage> = emptyList()

        fun recompute() {
            unreadCount = messages.count { it.isAdmin != viewerIsAdmin && it.timestamp > lastRead }
        }

        val readListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                lastRead = snap.getValue(Long::class.java) ?: 0L
                recompute()
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                messages = snap.toChatMessages()
                recompute()
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        readRef.addValueEventListener(readListener)
        messagesRef.addValueEventListener(messagesListener)

        onDispose {
            readRef.removeEventListener(readListener)
            messagesRef.removeEventListener(messagesListener)
        }
    }

    return unreadCount
}

/**
 * Live TOTAL unread count across every student's chat, from the admin's perspective.
 * Used for the admin's Messages tab badge.
 */
@Composable
fun rememberAdminTotalUnreadCount(): Int {
    val perStudentCounts = mutableStateMapOf<String, Int>()
    var studentIds by mutableStateOf<List<String>>(emptyList())

    // Keep the student roster live so newly added students are picked up automatically.
    DisposableEffect(Unit) {
        val registration = FirebaseFirestore.getInstance()
            .collection("students")
            .addSnapshotListener { snap, _ ->
                studentIds = snap?.documents?.map { it.id } ?: emptyList()
            }
        onDispose { registration.remove() }
    }

    // One unread-count listener pair per student, torn down and rebuilt whenever the roster changes.
    DisposableEffect(studentIds) {
        val db = FirebaseDatabase.getInstance()
        val cleanups = mutableListOf<() -> Unit>()

        studentIds.forEach { sid ->
            val messagesRef = db.getReference("chats/$sid")
            val readRef = db.getReference("chat_reads/$sid/admin")

            var lastRead = 0L
            var messages: List<ChatMessage> = emptyList()

            fun recompute() {
                perStudentCounts[sid] = messages.count { !it.isAdmin && it.timestamp > lastRead }
            }

            val readListener = object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    lastRead = snap.getValue(Long::class.java) ?: 0L
                    recompute()
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            val messagesListener = object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    messages = snap.toChatMessages()
                    recompute()
                }
                override fun onCancelled(error: DatabaseError) {}
            }

            readRef.addValueEventListener(readListener)
            messagesRef.addValueEventListener(messagesListener)

            cleanups.add {
                readRef.removeEventListener(readListener)
                messagesRef.removeEventListener(messagesListener)
            }
        }

        onDispose {
            perStudentCounts.clear()
            cleanups.forEach { it() }
        }
    }

    return perStudentCounts.values.sum()
}