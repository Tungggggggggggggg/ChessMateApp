package com.example.chessmate.ui.components

// Enum để biểu diễn màu của quân cờ
enum class PieceColor {
    WHITE, BLACK
}

// Enum để biểu diễn loại quân cờ
enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

// Lớp biểu diễn một quân cờ
data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val position: Position
)

// Lớp biểu diễn vị trí trên bàn cờ (row, col)
data class Position(val row: Int, val col: Int)

// Lớp biểu diễn trạng thái trò chơi
class ChessGame {
    private var board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }
    private var currentTurn: PieceColor = PieceColor.WHITE
    private var selectedPiece: ChessPiece? = null
    private var validMoves: List<Position> = emptyList()
    private var isGameOver: Boolean = false
    private var gameResult: String? = null

    init {
        initializeBoard()
    }

    // Khởi tạo bàn cờ với vị trí ban đầu của các quân cờ
    private fun initializeBoard() {
        // Quân trắng
        board[0][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 0))
        board[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 1))
        board[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 2))
        board[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE, Position(0, 3))
        board[0][4] = ChessPiece(PieceType.KING, PieceColor.WHITE, Position(0, 4))
        board[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 5))
        board[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 6))
        board[0][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 7))
        for (col in 0 until 8) {
            board[1][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE, Position(1, col))
        }

        // Quân đen
        board[7][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 0))
        board[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 1))
        board[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 2))
        board[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK, Position(7, 3))
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.BLACK, Position(7, 4))
        board[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 5))
        board[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 6))
        board[7][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 7))
        for (col in 0 until 8) {
            board[6][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK, Position(6, col))
        }
    }

    // Lấy quân cờ tại vị trí (row, col)
    fun getPieceAt(row: Int, col: Int): ChessPiece? {
        return board[row][col]
    }

    // Lấy danh sách các ô hợp lệ khi chọn một quân cờ
    fun getValidMoves(row: Int, col: Int): List<Position> {
        val piece = board[row][col] ?: return emptyList()
        if (piece.color != currentTurn) return emptyList()

        selectedPiece = piece
        validMoves = calculateValidMoves(piece)
        return validMoves
    }

    // Tính toán các nước đi hợp lệ cho một quân cờ
    private fun calculateValidMoves(piece: ChessPiece): List<Position> {
        val moves = mutableListOf<Position>()
        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) 1 else -1
                val startRow = if (piece.color == PieceColor.WHITE) 1 else 6

                // Di chuyển thẳng
                if (isInBounds(piece.position.row + direction, piece.position.col) &&
                    board[piece.position.row + direction][piece.position.col] == null) {
                    moves.add(Position(piece.position.row + direction, piece.position.col))
                    // Di chuyển 2 ô từ vị trí ban đầu
                    if (piece.position.row == startRow &&
                        board[piece.position.row + 2 * direction][piece.position.col] == null) {
                        moves.add(Position(piece.position.row + 2 * direction, piece.position.col))
                    }
                }

                // Bắt quân chéo
                for (colOffset in listOf(-1, 1)) {
                    val newRow = piece.position.row + direction
                    val newCol = piece.position.col + colOffset
                    if (isInBounds(newRow, newCol)) {
                        val targetPiece = board[newRow][newCol]
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(Position(newRow, newCol))
                        }
                    }
                }

                // En passant (tạm thời bỏ qua để đơn giản hóa, sẽ thêm sau)
            }
            PieceType.ROOK -> {
                // Di chuyển theo hàng và cột
                for (direction in listOf(-1, 1)) {
                    // Hàng
                    var r = piece.position.row + direction
                    while (isInBounds(r, piece.position.col)) {
                        if (board[r][piece.position.col] == null) {
                            moves.add(Position(r, piece.position.col))
                        } else {
                            if (board[r][piece.position.col]!!.color != piece.color) {
                                moves.add(Position(r, piece.position.col))
                            }
                            break
                        }
                        r += direction
                    }
                    // Cột
                    var c = piece.position.col + direction
                    while (isInBounds(piece.position.row, c)) {
                        if (board[piece.position.row][c] == null) {
                            moves.add(Position(piece.position.row, c))
                        } else {
                            if (board[piece.position.row][c]!!.color != piece.color) {
                                moves.add(Position(piece.position.row, c))
                            }
                            break
                        }
                        c += direction
                    }
                }
            }
            PieceType.KNIGHT -> {
                val knightMoves = listOf(
                    Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
                )
                for (move in knightMoves) {
                    val newRow = piece.position.row + move.first
                    val newCol = piece.position.col + move.second
                    if (isInBounds(newRow, newCol)) {
                        val targetPiece = board[newRow][newCol]
                        if (targetPiece == null || targetPiece.color != piece.color) {
                            moves.add(Position(newRow, newCol))
                        }
                    }
                }
            }
            PieceType.BISHOP -> {
                // Di chuyển theo đường chéo
                for (rowDir in listOf(-1, 1)) {
                    for (colDir in listOf(-1, 1)) {
                        var r = piece.position.row + rowDir
                        var c = piece.position.col + colDir
                        while (isInBounds(r, c)) {
                            if (board[r][c] == null) {
                                moves.add(Position(r, c))
                            } else {
                                if (board[r][c]!!.color != piece.color) {
                                    moves.add(Position(r, c))
                                }
                                break
                            }
                            r += rowDir
                            c += colDir
                        }
                    }
                }
            }
            PieceType.QUEEN -> {
                // Kết hợp của Rook và Bishop
                moves.addAll(calculateValidMoves(ChessPiece(PieceType.ROOK, piece.color, piece.position)))
                moves.addAll(calculateValidMoves(ChessPiece(PieceType.BISHOP, piece.color, piece.position)))
            }
            PieceType.KING -> {
                // Di chuyển 1 ô theo mọi hướng
                for (rowOffset in -1..1) {
                    for (colOffset in -1..1) {
                        if (rowOffset == 0 && colOffset == 0) continue
                        val newRow = piece.position.row + rowOffset
                        val newCol = piece.position.col + colOffset
                        if (isInBounds(newRow, newCol)) {
                            val targetPiece = board[newRow][newCol]
                            if (targetPiece == null || targetPiece.color != piece.color) {
                                moves.add(Position(newRow, newCol))
                            }
                        }
                    }
                }
                // Nhập thành (tạm thời bỏ qua để đơn giản hóa, sẽ thêm sau)
            }
        }
        return moves.filter { !movePutsKingInCheck(piece, it) }
    }

    // Kiểm tra xem nước đi có đặt vua vào thế chiếu không
    private fun movePutsKingInCheck(piece: ChessPiece, to: Position): Boolean {
        // Lưu trạng thái hiện tại
        val originalPiece = board[piece.position.row][piece.position.col]
        val targetPiece = board[to.row][to.col]

        // Thử di chuyển
        board[to.row][to.col] = piece.copy(position = to)
        board[piece.position.row][piece.position.col] = null

        // Kiểm tra xem vua có bị chiếu không
        val kingPosition = findKing(piece.color)
        val inCheck = isKingInCheck(piece.color, kingPosition)

        // Khôi phục trạng thái
        board[piece.position.row][piece.position.col] = originalPiece
        board[to.row][to.col] = targetPiece

        return inCheck
    }

    // Tìm vị trí của vua
    private fun findKing(color: PieceColor): Position {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return Position(row, col)
                }
            }
        }
        throw IllegalStateException("King not found for $color")
    }

    // Kiểm tra xem vua có bị chiếu không
    private fun isKingInCheck(color: PieceColor, kingPosition: Position): Boolean {
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = calculateValidMoves(piece)
                    if (moves.contains(kingPosition)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Kiểm tra xem vị trí có nằm trong bàn cờ không
    private fun isInBounds(row: Int, col: Int): Boolean {
        return row in 0 until 8 && col in 0 until 8
    }

    // Di chuyển quân cờ
    fun movePiece(to: Position) {
        val piece = selectedPiece ?: return
        if (to !in validMoves) return

        // Di chuyển quân cờ
        board[to.row][to.col] = piece.copy(position = to)
        board[piece.position.row][piece.position.col] = null

        // Đổi lượt
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        selectedPiece = null
        validMoves = emptyList()

        // Kiểm tra chiếu, chiếu hết, hòa
        checkGameState()
    }

    // Kiểm tra trạng thái trò chơi
    private fun checkGameState() {
        val kingPosition = findKing(currentTurn)
        if (isKingInCheck(currentTurn, kingPosition)) {
            // Kiểm tra chiếu hết
            if (isCheckmate()) {
                isGameOver = true
                gameResult = if (currentTurn == PieceColor.WHITE) "Black wins by checkmate!" else "White wins by checkmate!"
            }
        } else {
            // Kiểm tra hòa (stalemate)
            if (isStalemate()) {
                isGameOver = true
                gameResult = "Stalemate! Game is a draw."
            }
        }
    }

    // Kiểm tra chiếu hết
    private fun isCheckmate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == currentTurn) {
                    val moves = calculateValidMoves(piece)
                    for (move in moves) {
                        if (!movePutsKingInCheck(piece, move)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    // Kiểm tra hòa (stalemate)
    private fun isStalemate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == currentTurn) {
                    val moves = calculateValidMoves(piece)
                    if (moves.isNotEmpty()) {
                        return false
                    }
                }
            }
        }
        return true
    }

    // Lấy danh sách các ô hợp lệ hiện tại
    fun getHighlightedSquares(): List<Position> {
        return validMoves
    }

    // Lấy lượt hiện tại
    fun getCurrentTurn(): PieceColor {
        return currentTurn
    }

    // Kiểm tra trò chơi đã kết thúc chưa
    fun isGameOver(): Boolean {
        return isGameOver
    }

    // Lấy kết quả trò chơi
    fun getGameResult(): String? {
        return gameResult
    }
}