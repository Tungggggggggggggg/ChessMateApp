package com.example.chessmate.model

/**
 * Lớp dữ liệu đại diện cho một yêu cầu kết bạn giữa các người dùng.
 *
 * @property fromUserId Định danh duy nhất của người dùng gửi yêu cầu kết bạn.
 * @property fromName Tên của người dùng gửi yêu cầu kết bạn.
 * @property toUserId Định danh duy nhất của người dùng nhận yêu cầu kết bạn.
 */
data class FriendRequest(
    val fromUserId: String,
    val fromName: String,
    val toUserId: String
)

/**
 * Lớp dữ liệu đại diện cho một người dùng trong ứng dụng cờ vua.
 *
 * @property userId Định danh duy nhất của người dùng.
 * @property name Tên hiển thị của người dùng.
 * @property email Địa chỉ email của người dùng.
 * @property isOnline Cho biết người dùng hiện tại có đang trực tuyến hay không (mặc định là false).
 */
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val isOnline: Boolean = false
)