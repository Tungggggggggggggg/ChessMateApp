package com.example.chessmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chessmate.model.FriendRequest
import com.example.chessmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FindFriendsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _receivedRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedRequests: StateFlow<List<FriendRequest>> = _receivedRequests

    private val _sentRequests = MutableStateFlow<List<String>>(emptyList())
    val sentRequests: StateFlow<List<String>> = _sentRequests

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends

    private var friendsListener1: ListenerRegistration? = null
    private var friendsListener2: ListenerRegistration? = null
    private var receivedRequestsListener: ListenerRegistration? = null
    private var sentRequestsListener: ListenerRegistration? = null

    init {
        loadFriends()
        loadReceivedRequests()
        loadSentRequests()
    }

    private fun normalizeQuery(query: String): String {
        return java.text.Normalizer.normalize(query.lowercase(), java.text.Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            Log.d("FindFriendsViewModel", "Search query is blank, returning empty results")
            _searchResults.value = emptyList()
            return
        }
        val normalizedQuery = normalizeQuery(query).trim()
        Log.d("FindFriendsViewModel", "Searching for query: '$normalizedQuery'")

        firestore.collection("users")
            .whereArrayContains("nameKeywords", normalizedQuery)
            .get()
            .addOnSuccessListener { result ->
                Log.d("FindFriendsViewModel", "Firestore query returned ${result.documents.size} documents")
                val users = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: return@mapNotNull null
                    val userId = doc.getString("userId") ?: doc.id
                    val nameKeywords = doc.get("nameKeywords") as? List<String> ?: emptyList()
                    val currentUid = auth.currentUser?.uid
                    if (userId == currentUid) return@mapNotNull null
                    Log.d("FindFriendsViewModel", "Found user: $userId, name: $name, nameKeywords: $nameKeywords")
                    User(userId, name, email)
                }
                Log.d("FindFriendsViewModel", "Search results: ${users.size} users found")
                _searchResults.value = users
            }
            .addOnFailureListener { exception ->
                Log.e("FindFriendsViewModel", "Search failed: ${exception.message}")
                _searchResults.value = emptyList()
            }
    }

    fun sendFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(fromUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fromUserName = document.getString("name") ?: "Unknown"
                    val request = hashMapOf(
                        "fromUserId" to fromUserId,
                        "fromName" to fromUserName,
                        "toUserId" to toUserId,
                        "timestamp" to com.google.firebase.Timestamp.now(),
                        "status" to "pending"
                    )
                    firestore.collection("friend_requests")
                        .add(request)
                        .addOnSuccessListener {
                            _sentRequests.value += toUserId
                            Log.d("FindFriendsViewModel", "Friend request sent to $toUserId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FindFriendsViewModel", "Error sending friend request: ${e.message}")
                        }
                } else {
                    Log.e("FindFriendsViewModel", "User document not found for $fromUserId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindFriendsViewModel", "Error fetching user name: ${e.message}")
            }
    }

    fun cancelFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid ?: return
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", fromUserId)
            .whereEqualTo("toUserId", toUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                requestDoc?.reference?.delete()?.addOnSuccessListener {
                    _sentRequests.value = _sentRequests.value.filter { it != toUserId }
                    Log.d("FindFriendsViewModel", "Friend request to $toUserId canceled")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindFriendsViewModel", "Error canceling friend request: ${e.message}")
            }
    }

    fun removeFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friends")
            .whereEqualTo("user1", currentUserId)
            .whereEqualTo("user2", friendId)
            .get()
            .addOnSuccessListener { result1 ->
                val doc1 = result1.documents.firstOrNull()
                if (doc1 != null) {
                    doc1.reference.delete().addOnSuccessListener {
                        _friends.value = _friends.value.filter { it.userId != friendId }
                        Log.d("FindFriendsViewModel", "Friend $friendId removed (user1)")
                    }
                } else {
                    firestore.collection("friends")
                        .whereEqualTo("user1", friendId)
                        .whereEqualTo("user2", currentUserId)
                        .get()
                        .addOnSuccessListener { result2 ->
                            val doc2 = result2.documents.firstOrNull()
                            doc2?.reference?.delete()?.addOnSuccessListener {
                                _friends.value = _friends.value.filter { it.userId != friendId }
                                Log.d("FindFriendsViewModel", "Friend $friendId removed (user2)")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FindFriendsViewModel", "Error removing friend (user2): ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindFriendsViewModel", "Error removing friend (user1): ${e.message}")
            }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", request.fromUserId)
            .whereEqualTo("toUserId", request.toUserId)
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                requestDoc?.reference?.update("status", "accepted")?.addOnSuccessListener {
                    // Thêm mối quan hệ bạn bè
                    firestore.collection("friends")
                        .add(
                            hashMapOf(
                                "user1" to request.fromUserId,
                                "user2" to request.toUserId
                            )
                        )
                        .addOnSuccessListener {
                            _friends.value += User(
                                request.fromUserId,
                                request.fromName,
                                ""
                            )
                            Log.d("FindFriendsViewModel", "Friend request accepted: ${request.fromUserId}")

                            // Kiểm tra và xóa lời mời từ currentUserId đến fromUserId (nếu có)
                            firestore.collection("friend_requests")
                                .whereEqualTo("fromUserId", currentUserId)
                                .whereEqualTo("toUserId", request.fromUserId)
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener { sentRequestResult ->
                                    val sentRequestDoc = sentRequestResult.documents.firstOrNull()
                                    sentRequestDoc?.reference?.delete()?.addOnSuccessListener {
                                        Log.d("FindFriendsViewModel", "Deleted sent friend request from $currentUserId to ${request.fromUserId} after acceptance")
                                    }?.addOnFailureListener { e ->
                                        Log.e("FindFriendsViewModel", "Error deleting sent friend request: ${e.message}")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FindFriendsViewModel", "Error checking sent friend request: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FindFriendsViewModel", "Error adding friend: ${e.message}")
                        }

                    // Xóa lời mời đã chấp nhận
                    requestDoc.reference.delete()
                        .addOnSuccessListener {
                            Log.d("FindFriendsViewModel", "Friend request deleted after acceptance")
                        }
                }?.addOnFailureListener { e ->
                    Log.e("FindFriendsViewModel", "Error updating friend request status: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindFriendsViewModel", "Error accepting friend request: ${e.message}")
            }
    }

    fun declineFriendRequest(request: FriendRequest) {
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", request.fromUserId)
            .whereEqualTo("toUserId", request.toUserId)
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                requestDoc?.reference?.update("status", "declined")
                    ?.addOnSuccessListener {
                        Log.d("FindFriendsViewModel", "Friend request declined: ${request.fromUserId}")
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("FindFriendsViewModel", "Error declining friend request: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FindFriendsViewModel", "Error declining friend request: ${e.message}")
            }
    }

    fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        friendsListener1?.remove()
        friendsListener2?.remove()

        friendsListener1 = firestore.collection("friends")
            .whereEqualTo("user1", currentUserId)
            .addSnapshotListener { snapshot1, error1 ->
                if (error1 != null || snapshot1 == null) {
                    Log.e("FindFriendsViewModel", "Error loading friends (user1): ${error1?.message}")
                    return@addSnapshotListener
                }

                val fetchedFriends = mutableSetOf<User>()
                val allDocs = snapshot1.documents

                friendsListener2 = firestore.collection("friends")
                    .whereEqualTo("user2", currentUserId)
                    .addSnapshotListener { snapshot2, error2 ->
                        if (error2 != null || snapshot2 == null) {
                            Log.e("FindFriendsViewModel", "Error loading friends (user2): ${error2?.message}")
                            return@addSnapshotListener
                        }

                        allDocs += snapshot2.documents

                        allDocs.forEach { doc ->
                            val user1 = doc.getString("user1")
                            val user2 = doc.getString("user2")
                            val friendId = if (user1 == currentUserId) user2 else user1
                            if (!friendId.isNullOrBlank()) {
                                firestore.collection("users").document(friendId).get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc.exists()) {
                                            val name = userDoc.getString("name") ?: "Unknown"
                                            val email = userDoc.getString("email") ?: ""
                                            fetchedFriends.add(User(friendId, name, email))
                                            _friends.value = fetchedFriends.toList()
                                            Log.d("FindFriendsViewModel", "Friend updated: $friendId, name: $name")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FindFriendsViewModel", "Error fetching user $friendId: ${e.message}")
                                    }
                            }
                        }

                        if (allDocs.isEmpty()) {
                            _friends.value = emptyList()
                            Log.d("FindFriendsViewModel", "No friends found")
                        }
                    }
            }
    }

    fun loadReceivedRequests() {
        val currentUserId = auth.currentUser?.uid ?: return

        receivedRequestsListener?.remove()

        receivedRequestsListener = firestore.collection("friend_requests")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("FindFriendsViewModel", "Error loading received requests: ${error?.message}")
                    _receivedRequests.value = emptyList()
                    return@addSnapshotListener
                }

                val friendRequests = snapshot.documents.mapNotNull { doc ->
                    val fromUserId = doc.getString("fromUserId") ?: return@mapNotNull null
                    val fromName = doc.getString("fromName") ?: "Unknown"
                    FriendRequest(fromUserId, fromName, currentUserId)
                }
                _receivedRequests.value = friendRequests
                Log.d("FindFriendsViewModel", "Received requests updated: ${friendRequests.size} requests")
            }
    }

    fun loadSentRequests() {
        val currentUserId = auth.currentUser?.uid ?: return

        sentRequestsListener?.remove()

        sentRequestsListener = firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("FindFriendsViewModel", "Error loading sent requests: ${error?.message}")
                    _sentRequests.value = emptyList()
                    return@addSnapshotListener
                }

                val sentRequestsList = snapshot.documents.mapNotNull { doc ->
                    doc.getString("toUserId")
                }
                _sentRequests.value = sentRequestsList
                Log.d("FindFriendsViewModel", "Sent requests updated: ${sentRequestsList.size} requests")
            }
    }

    override fun onCleared() {
        super.onCleared()
        friendsListener1?.remove()
        friendsListener2?.remove()
        receivedRequestsListener?.remove()
        sentRequestsListener?.remove()
        Log.d("FindFriendsViewModel", "ViewModel cleared, listeners removed")
    }
}