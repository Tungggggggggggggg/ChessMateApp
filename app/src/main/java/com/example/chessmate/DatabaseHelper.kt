package com.example.chessmate

import com.example.chessmate.ui.screen.Match
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object DatabaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://chessmate-2c597-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val rootRef: DatabaseReference = database.reference

    // Tham chiếu đến node "users"
    val usersRef: DatabaseReference = rootRef.child("users")

    // Tham chiếu đến node "match_history" (lịch sử trận đấu của từng user)
    private val matchHistoryRef: DatabaseReference = rootRef.child("match_history")

    // Thêm hoặc cập nhật thông tin user
    fun addUser(
        userId: String,
        name: String,
        username: String,
        email: String? = null,
        createdAt: String = "",
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val userData = mapOf(
            "name" to name,
            "username" to username,
            "email" to email,
            "createdAt" to createdAt,
            "description" to "",
            "rating" to 1200
        )
        usersRef.child(userId).setValue(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    // Đọc dữ liệu user
    fun getUser(userId: String, onResult: (Map<String, Any>?) -> Unit) {
        usersRef.child(userId).get().addOnSuccessListener { snapshot ->
            onResult(snapshot.value as? Map<String, Any>)
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // Cập nhật mô tả của user
    fun updateUserDescription(userId: String, description: String) {
        usersRef.child(userId).child("description").setValue(description)
    }

    // Thêm một trận đấu vào lịch sử của user
    fun addMatch(userId: String, match: Match) {
        val matchData = mapOf(
            "result" to match.result,
            "date" to match.date,
            "moves" to match.moves,
            "opponent" to match.opponent
        )
        // Lưu trận đấu vào node match_history/<userId>/<matchId>
        val matchId = matchHistoryRef.child(userId).push().key ?: return
        matchHistoryRef.child(userId).child(matchId).setValue(matchData)
    }

    // Đọc lịch sử trận đấu của user
    fun getMatchHistory(userId: String, onResult: (List<Match>) -> Unit) {
        matchHistoryRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matches = mutableListOf<Match>()
                for (matchSnapshot in snapshot.children) {
                    val result = matchSnapshot.child("result").getValue(String::class.java) ?: ""
                    val date = matchSnapshot.child("date").getValue(String::class.java) ?: ""
                    val moves = matchSnapshot.child("moves").getValue(Int::class.java) ?: 0
                    val opponent = matchSnapshot.child("opponent").getValue(String::class.java) ?: ""
                    matches.add(Match(result, date, moves, opponent))
                }
                onResult(matches)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}