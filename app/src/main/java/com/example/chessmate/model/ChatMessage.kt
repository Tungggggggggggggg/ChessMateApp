package com.example.chessmate.model

/**
 * Lớp dữ liệu đại diện cho một tin nhắn trò chuyện trong ứng dụng cờ vua.
 *
 * @property senderId Định danh duy nhất của người dùng gửi tin nhắn.
 * @property message Nội dung của tin nhắn trò chuyện.
 * @property timestamp Thời gian gửi tin nhắn, được biểu diễn dưới dạng dấu thời gian Unix.
 * @property sequence Số thứ tự để sắp xếp các tin nhắn (mặc định là 0).
 * @property readBy Danh sách các ID người dùng đã đọc tin nhắn (mặc định là danh sách rỗng).
 */
data class ChatMessage(
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val sequence: Long = 0,
    val readBy: List<String> = emptyList()
)