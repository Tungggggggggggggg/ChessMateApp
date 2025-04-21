package com.example.chessmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chessmate.model.ChatMessage
import com.example.chessmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

/**
 * Dữ liệu kết hợp bạn bè với tin nhắn cuối cùng và trạng thái chưa đọc.
 */
data class FriendWithLastMessage(
    val friend: User,
    val lastMessage: ChatMessage? = null,
    val hasUnread: Boolean = false
)

/**
 * ViewModel quản lý chức năng trò chuyện, bao gồm tải danh sách bạn bè, tin nhắn và trạng thái chưa đọc.
 */
class ChatViewModel : ViewModel() {
    // Danh sách bạn bè với tin nhắn cuối cùng
    private val _friendsWithMessages = MutableStateFlow<List<FriendWithLastMessage>>(emptyList())
    val friendsWithMessages: StateFlow<List<FriendWithLastMessage>> get() = _friendsWithMessages

    // Danh sách tin nhắn trong cuộc trò chuyện hiện tại
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> get() = _messages

    // Trạng thái có tin nhắn chưa đọc
    private val _hasUnreadMessages = MutableStateFlow(false)
    val hasUnreadMessages: StateFlow<Boolean> get() = _hasUnreadMessages

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var messageSequence = 0L
    private var chatListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null
    private var currentConversationId: String? = null

    // ID của người dùng hiện tại
    val currentUserId: String? get() = auth.currentUser?.uid

    init {
        loadFriendsWithMessages()
    }

    /**
     * Tải danh sách bạn bè cùng với tin nhắn cuối cùng và trạng thái chưa đọc.
     */
    fun loadFriendsWithMessages() {
        val currentUserId = auth.currentUser?.uid ?: return

        friendsListener?.remove()

        friendsListener = firestore.collection("friends")
            .whereIn("user1", listOf(currentUserId))
            .addSnapshotListener { result1, error1 ->
                if (error1 != null || result1 == null) {
                    Log.e("ChatViewModel", "Error loading friends (user1): ${error1?.message}")
                    return@addSnapshotListener
                }

                firestore.collection("friends")
                    .whereIn("user2", listOf(currentUserId))
                    .addSnapshotListener { result2, error2 ->
                        if (error2 != null || result2 == null) {
                            Log.e("ChatViewModel", "Error loading friends (user2): ${error2?.message}")
                            return@addSnapshotListener
                        }

                        val allDocs = result1.documents + result2.documents
                        val friendList = mutableListOf<FriendWithLastMessage>()
                        val currentFriendIds = mutableSetOf<String>()

                        allDocs.forEach { doc ->
                            val user1 = doc.getString("user1")
                            val user2 = doc.getString("user2")
                            val friendId = if (user1 == currentUserId) user2 else user1
                            if (!friendId.isNullOrBlank()) {
                                currentFriendIds.add(friendId)
                                firestore.collection("users").document(friendId).get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc.exists()) {
                                            val name = userDoc.getString("name") ?: "Không xác định"
                                            val email = userDoc.getString("email") ?: ""
                                            val isOnline = userDoc.getBoolean("isOnline") ?: false
                                            val friend = User(friendId, name, email, isOnline)

                                            val conversationId = getConversationId(currentUserId, friendId)
                                            firestore.collection("conversations")
                                                .document(conversationId)
                                                .collection("chat_messages")
                                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                                .limit(1)
                                                .addSnapshotListener { snapshot, error ->
                                                    if (error != null || snapshot == null) {
                                                        Log.e("ChatViewModel", "Error loading last message: ${error?.message}")
                                                        return@addSnapshotListener
                                                    }

                                                    val lastMessage = snapshot.documents.firstOrNull()?.let { doc ->
                                                        val senderId = doc.getString("senderId") ?: return@let null
                                                        val message = doc.getString("message") ?: return@let null
                                                        val timestamp = doc.getLong("timestamp") ?: return@let null
                                                        val sequence = doc.getLong("sequence") ?: 0L
                                                        val readBy = doc.get("readBy") as? List<String> ?: emptyList()
                                                        ChatMessage(senderId, message, timestamp, sequence, readBy)
                                                    }

                                                    val hasUnread = lastMessage?.let { msg ->
                                                        msg.senderId != currentUserId && !msg.readBy.contains(currentUserId)
                                                    } ?: false

                                                    if (friendId in currentFriendIds) {
                                                        friendList.removeAll { it.friend.userId == friendId }
                                                        friendList.add(FriendWithLastMessage(friend, lastMessage, hasUnread))
                                                        _friendsWithMessages.value = friendList.sortedBy { it.lastMessage?.timestamp ?: 0L }.reversed()
                                                    }

                                                    _hasUnreadMessages.value = friendList.any { it.hasUnread }
                                                    Log.d("ChatViewModel", "Loaded friend: ID=$friendId, Name=$name, LastMessage=${lastMessage?.message}, Unread=$hasUnread")
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ChatViewModel", "Error fetching user $friendId: ${e.message}")
                                    }
                            }
                        }

                        friendList.removeAll { it.friend.userId !in currentFriendIds }
                        _friendsWithMessages.value = friendList.sortedBy { it.lastMessage?.timestamp ?: 0L }.reversed()

                        if (allDocs.isEmpty()) {
                            _friendsWithMessages.value = emptyList()
                            _hasUnreadMessages.value = false
                            Log.d("ChatViewModel", "No friends found")
                        }
                    }
            }
    }

    /**
     * Lắng nghe tin nhắn trong cuộc trò chuyện với một người bạn.
     *
     * @param friendId ID của người bạn trong cuộc trò chuyện.
     */
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

                loadFriendsWithMessages()
                Log.d("ChatViewModel", "Messages loaded: ${messages.size}")
            }
    }

    /**
     * Gửi tin nhắn tới một người bạn.
     *
     * @param friendId ID của người bạn nhận tin nhắn.
     * @param message Nội dung tin nhắn.
     */
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

    /**
     * Đánh dấu các tin nhắn trong cuộc trò chuyện với một người bạn là đã đọc.
     *
     * @param friendId ID của người bạn trong cuộc trò chuyện.
     */
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
                        batch.update(doc.reference, "readBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                    }
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("ChatViewModel", "Marked messages as read for conversation: $conversationId")
                        loadFriendsWithMessages()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "Error marking messages as read: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching messages to mark as read: ${e.message}")
            }
    }

    /**
     * Tạo ID cuộc trò chuyện dựa trên ID của hai người dùng.
     *
     * @param userId1 ID của người dùng thứ nhất.
     * @param userId2 ID của người dùng thứ hai.
     * @return ID cuộc trò chuyện duy nhất.
     */
    fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    /**
     * Dọn dẹp các listener khi ViewModel bị hủy.
     */
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
        friendsListener?.remove()
        currentConversationId = null
    }
}

/**
 * Factory để tạo ChatViewModel.
 */
class ChatViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}