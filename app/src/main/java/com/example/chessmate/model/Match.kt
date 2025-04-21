package com.example.chessmate.model

/**
 * Lớp dữ liệu đại diện cho một trận đấu cờ vua đã hoàn thành.
 *
 * @property result Kết quả của trận đấu (ví dụ: "Trắng thắng", "Hòa").
 * @property date Ngày diễn ra trận đấu, được lưu dưới dạng chuỗi.
 * @property moves Tổng số nước đi được thực hiện trong trận đấu.
 * @property opponent Tên hoặc ID của đối thủ.
 */
data class Match(
    val result: String,
    val date: String,
    val moves: Int,
    val opponent: String
)