package com.example.chessmate.model

/**
 * Lớp dữ liệu đại diện cho một vị trí trên bàn cờ.
 *
 * @property row Chỉ số hàng (0 đến 7, trong đó 0 là hàng đầu tiên của bên Trắng).
 * @property col Chỉ số cột (0 đến 7, trong đó 0 là cột 'a').
 */
data class Position(val row: Int, val col: Int)