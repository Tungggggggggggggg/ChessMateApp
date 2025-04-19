package com.example.chessmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chessmate.model.ChatMessage
import com.example.chessmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

class ChatViewModel : ViewModel() {
    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> get() = _friends

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> get() = _messages

    private val _hasUnreadMessages = MutableStateFlow(false)
    val hasUnreadMessages: StateFlow<Boolean> get() = _hasUnreadMessages

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var messageSequence = 0L
    private var chatListener: ListenerRegistration? = null
    private var currentConversationId: String? = null

    // Thuộc tính công khai để lấy currentUserId
    val currentUserId: String? get() = auth.currentUser?.uid

    init {
        loadFriends()
    }

    fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friends")
            .whereIn("user1", listOf(currentUserId))
            .get()
            .addOnSuccessListener { result1 ->
                firestore.collection("friends")
                    .whereIn("user2", listOf(currentUserId))
                    .get()
                    .addOnSuccessListener { result2 ->
                        val allDocs = result1.documents + result2.documents
                        val fetchedFriends = mutableSetOf<User>()
                        allDocs.forEach { doc ->
                            val user1 = doc.getString("user1")
                            val user2 = doc.getString("user2")
                            val friendId = if (user1 == currentUserId) user2 else user1
                            if (!friendId.isNullOrBlank()) {
                                firestore.collection("users").document(friendId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val name = userDoc.getString("name") ?: "Không xác định"
                                        val email = userDoc.getString("email") ?: ""
                                        val isOnline = userDoc.getBoolean("isOnline") ?: false
                                        fetchedFriends.add(User(friendId, name, email, isOnline))
                                        _friends.value = fetchedFriends.toList()
                                        Log.d("ChatViewModel", "Loaded friend: ID=$friendId, Name=$name")
                                    }
                            }
                        }
                    }
            }
    }

    fun listenToChatMessages(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val conversationId = getConversationId(currentUserId, friendId)
        if (conversationId == currentConversationId) {
            Log.d("ChatViewModel", "Listener already attached for $conversationId")
            return
        }
        currentConversationId = conversationId
        Log.d("ChatViewModel", "Attaching snapshot listener for conversationId: $conversationId")

        chatListener?.remove()
        chatListener = firestore.collection("conversations")
            .document(conversationId)
            .collection("chat_messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("ChatViewModel", "Error loading messages: ${error?.message}")
                    return@addSnapshotListener
                }
                Log.d("ChatViewModel", "Snapshot received, documents: ${snapshot.documents.size}")
                val messages = mutableListOf<ChatMessage>()
                snapshot.documents.forEach { doc ->
                    val senderId = doc.getString("senderId") ?: return@forEach
                    val message = doc.getString("message") ?: return@forEach
                    val timestamp = doc.getLong("timestamp") ?: return@forEach
                    val sequence = doc.getLong("sequence") ?: 0L
                    val readBy = doc.get("readBy") as? List<String> ?: emptyList()
                    messages.add(ChatMessage(senderId, message, timestamp, sequence, readBy))
                }
                messages.sortWith(compareBy({ it.timestamp }, { it.sequence }))
                _messages.value = messages

                _hasUnreadMessages.value = messages.any { message ->
                    message.senderId != currentUserId && !message.readBy.contains(currentUserId)
                }
                Log.d("ChatViewModel", "Messages loaded: ${messages.size}")
            }
    }

    fun sendMessage(friendId: String, message: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (message.trim().isEmpty() || message.length > 200) return
        val conversationId = getConversationId(currentUserId, friendId)
        val chatData = hashMapOf(
            "senderId" to currentUserId,
            "message" to message.trim(),
            "timestamp" to System.currentTimeMillis(),
            "sequence" to messageSequence++,
            "readBy" to listOf(currentUserId)
        )

        firestore.collection("conversations")
            .document(conversationId)
            .collection("chat_messages")
            .add(chatData)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent: $message")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
    }

    fun markMessagesAsRead(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val conversationId = getConversationId(currentUserId, friendId)

        firestore.collection("conversations")
            .document(conversationId)
            .collection("chat_messages")
            .whereNotEqualTo("senderId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    val readBy = doc.get("readBy") as? List<String> ?: emptyList()
                    if (currentUserId !in readBy) {
                        batch.update(doc.reference, "readBy", FieldValue.arrayUnion(currentUserId))
                    }
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("ChatViewModel", "Marked messages as read for conversation: $conversationId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "Error marking messages as read: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching messages to mark as read: ${e.message}")
            }
    }

    fun removeFriend(friend: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friends")
            .whereEqualTo("user1", currentUserId)
            .whereEqualTo("user2", friend.userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.firstOrNull()?.reference?.delete()?.addOnSuccessListener {
                    _friends.value = _friends.value.filter { it.userId != friend.userId }
                    Log.d("ChatViewModel", "Removed friend: ID=${friend.userId}, Name=${friend.name}")
                }
            }
        firestore.collection("friends")
            .whereEqualTo("user1", friend.userId)
            .whereEqualTo("user2", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.firstOrNull()?.reference?.delete()?.addOnSuccessListener {
                    _friends.value = _friends.value.filter { it.userId != friend.userId }
                    Log.d("ChatViewModel", "Removed friend: ID=${friend.userId}, Name=${friend.name}")
                }
            }
    }

    fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
        currentConversationId = null
    }
}

class ChatViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}