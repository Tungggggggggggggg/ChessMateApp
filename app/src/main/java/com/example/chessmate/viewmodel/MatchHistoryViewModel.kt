package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.Match
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý lịch sử trận đấu của người dùng, tải dữ liệu từ Firestore.
 */
class MatchHistoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Danh sách các trận đấu
    val matches = mutableStateOf<List<Match>>(emptyList())
    // Trạng thái đang tải
    val isLoading = mutableStateOf(true)
    // Thông báo lỗi (nếu có)
    val error = mutableStateOf<String?>(null)

    /**
     * Tải lịch sử trận đấu của một người dùng từ Firestore.
     *
     * @param userId ID của người dùng cần tải lịch sử.
     */
    fun loadMatchHistory(userId: String) {
        // Kiểm tra nếu userId rỗng
        if (userId.isEmpty()) {
            error.value = "Người dùng không hợp lệ."
            isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("match_history")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val matchList = mutableListOf<Match>()
                        // Chuyển đổi dữ liệu Firestore thành danh sách Match
                        for (document in querySnapshot) {
                            val result = document.getString("result") ?: ""
                            val date = document.getString("date") ?: ""
                            val moves = document.getLong("moves")?.toInt() ?: 0
                            val opponent = document.getString("opponent") ?: ""
                            matchList.add(Match(result, date, moves, opponent))
                        }
                        matches.value = matchList
                        isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        error.value = "Lỗi khi tải lịch sử: ${e.message}"
                        isLoading.value = false
                    }
            } catch (e: Exception) {
                error.value = "Lỗi không xác định: ${e.message}"
                isLoading.value = false
            }
        }
    }
}