package com.example.chessmate.model

/**
 * Lớp enum đại diện cho các màu sắc có thể có của quân cờ.
 */
enum class PieceColor {
    WHITE, // Đại diện cho quân cờ màu trắng
    BLACK  // Đại diện cho quân cờ màu đen
}

/**
 * Lớp enum đại diện cho các loại quân cờ khác nhau.
 */
enum class PieceType {
    PAWN,   // Đại diện cho quân Tốt
    ROOK,   // Đại diện cho quân Xe
    KNIGHT, // Đại diện cho quân Mã
    BISHOP, // Đại diện cho quân Tượng
    QUEEN,  // Đại diện cho quân Hậu
    KING    // Đại diện cho quân Vua
}

/**
 * Lớp dữ liệu đại diện cho một quân cờ với loại, màu sắc và vị trí trên bàn cờ.
 *
 * @property type Loại của quân cờ (ví dụ: Tốt, Vua).
 * @property color Màu sắc của quân cờ (TRẮNG hoặc ĐEN).
 * @property position Vị trí hiện tại của quân cờ trên bàn cờ.
 */
data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val position: Position
)

/**
 * Lớp dữ liệu đại diện cho một nước đi trên bàn cờ.
 *
 * @property from Vị trí bắt đầu của quân cờ được di chuyển.
 * @property position Vị trí đích của nước đi.
 * @property captures Cho biết liệu nước đi có dẫn đến việc ăn quân của đối thủ hay không.
 */
data class Move(
    val from: Position,
    val position: Position,
    val captures: Boolean
)