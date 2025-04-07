package com.example.chessmate.model

data class Move(val position: Position, val captures: Boolean)

class ChessGame {
    private var board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }
    private var currentTurn: PieceColor = PieceColor.WHITE
    private var selectedPiece: ChessPiece? = null
    private var validMoves: List<Position> = emptyList()
    private var isGameOver: Boolean = false
    private var gameResult: String? = null
    private var lastMove: Pair<Position, Position>? = null
    private var pendingPromotion: Position? = null
    private val hasMoved: MutableMap<String, Boolean> = mutableMapOf(
        "white_king" to false,
        "white_kingside_rook" to false,
        "white_queenside_rook" to false,
        "black_king" to false,
        "black_kingside_rook" to false,
        "black_queenside_rook" to false
    )

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
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

    fun getPieceAt(row: Int, col: Int): ChessPiece? = board[row][col]

    fun selectPiece(row: Int, col: Int): List<Move> {
        val piece = board[row][col] ?: return emptyList()
        if (piece.color != currentTurn) return emptyList()
        selectedPiece = piece
        val moves = calculateValidMoves(piece)
        validMoves = moves.map { it.position }
        return moves
    }

    fun movePiece(to: Position): Boolean {
        val piece = selectedPiece ?: return false
        val validMove = calculateValidMoves(piece).find { it.position == to } ?: return false

        val targetPiece = board[to.row][to.col]
        if (targetPiece != null && targetPiece.color == piece.color && targetPiece.type == PieceType.KING) {
            return false
        }

        if (piece.type == PieceType.PAWN && (to.row == 7 || to.row == 0)) {
            board[to.row][to.col] = piece.copy(position = to)
            board[piece.position.row][piece.position.col] = null
            pendingPromotion = to
            return true
        }

        val isEnPassant = piece.type == PieceType.PAWN && board[to.row][to.col] == null &&
                to.col != piece.position.col && lastMove?.let { last ->
            last.second.row == piece.position.row &&
                    last.second.col == to.col &&
                    board[last.second.row][last.second.col]?.type == PieceType.PAWN &&
                    board[last.second.row][last.second.col]?.color != piece.color &&
                    kotlin.math.abs(last.first.row - last.second.row) == 2
        } == true

        val isCastling = piece.type == PieceType.KING && kotlin.math.abs(to.col - piece.position.col) == 2
        val rookCol = if (to.col > piece.position.col) 7 else 0
        val rookNewCol = if (to.col > piece.position.col) 5 else 3
        val row = piece.position.row

        val from = piece.position
        board[to.row][to.col] = piece.copy(position = to)
        board[piece.position.row][piece.position.col] = null

        if (isEnPassant) {
            lastMove?.let { last ->
                board[last.second.row][last.second.col] = null
            }
        }

        if (isCastling) {
            val rook = board[row][rookCol]
            if (rook != null) {
                board[row][rookCol] = null
                board[row][rookNewCol] = rook.copy(position = Position(row, rookNewCol))
            }
        }

        if (piece.type == PieceType.KING) {
            hasMoved["${piece.color}_king"] = true
        }
        if (piece.type == PieceType.ROOK) {
            if (piece.position.col == 0) hasMoved["${piece.color}_queenside_rook"] = true
            if (piece.position.col == 7) hasMoved["${piece.color}_kingside_rook"] = true
        }

        lastMove = Pair(from, to)
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        selectedPiece = null
        validMoves = emptyList()
        checkGameState()
        return true
    }

    fun promotePawn(toType: PieceType) {
        pendingPromotion?.let { pos ->
            val piece = board[pos.row][pos.col]
            if (piece != null && piece.type == PieceType.PAWN) {
                board[pos.row][pos.col] = ChessPiece(toType, piece.color, pos)
            }
            pendingPromotion = null
            currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            checkGameState()
        }
    }

    private fun calculateValidMoves(piece: ChessPiece): List<Move> {
        val rawMoves = calculateRawMoves(piece)
        val validMoves = if (hasKing(piece.color)) {
            rawMoves.filter { !movePutsKingInCheck(piece, it.position) }
        } else {
            rawMoves
        }

        if (piece.type == PieceType.KING) {
            val castlingMoves = calculateCastlingMoves(piece)
            return validMoves + castlingMoves
        }
        return validMoves
    }

    private fun calculateRawMoves(piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) 1 else -1
                val startRow = if (piece.color == PieceColor.WHITE) 1 else 6
                val targetRow = piece.position.row + direction
                if (isInBounds(targetRow, piece.position.col) &&
                    board[targetRow][piece.position.col] == null
                ) {
                    moves.add(Move(Position(targetRow, piece.position.col), false))
                    if (piece.position.row == startRow &&
                        isInBounds(piece.position.row + 2 * direction, piece.position.col) &&
                        board[piece.position.row + 2 * direction][piece.position.col] == null
                    ) {
                        moves.add(Move(Position(piece.position.row + 2 * direction, piece.position.col), false))
                    }
                }
                for (colOffset in listOf(-1, 1)) {
                    val newRow = piece.position.row + direction
                    val newCol = piece.position.col + colOffset
                    if (isInBounds(newRow, newCol)) {
                        val targetPiece = board[newRow][newCol]
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(Move(Position(newRow, newCol), true))
                        }
                        lastMove?.let { last ->
                            if (last.second.row == piece.position.row &&
                                last.second.col == newCol &&
                                board[last.second.row][last.second.col]?.type == PieceType.PAWN &&
                                board[last.second.row][last.second.col]?.color != piece.color &&
                                kotlin.math.abs(last.first.row - last.second.row) == 2
                            ) {
                                moves.add(Move(Position(newRow, newCol), true))
                            }
                        }
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
                        moves.add(Move(Position(newRow, newCol), targetPiece != null && targetPiece.color != piece.color))
                    }
                }
            }
            PieceType.ROOK -> {
                for (direction in listOf(-1, 1)) {
                    var r = piece.position.row + direction
                    while (isInBounds(r, piece.position.col)) {
                        val targetPiece = board[r][piece.position.col]
                        moves.add(Move(Position(r, piece.position.col), targetPiece != null && targetPiece.color != piece.color))
                        if (targetPiece != null) break
                        r += direction
                    }
                    var c = piece.position.col + direction
                    while (isInBounds(piece.position.row, c)) {
                        val targetPiece = board[piece.position.row][c]
                        moves.add(Move(Position(piece.position.row, c), targetPiece != null && targetPiece.color != piece.color))
                        if (targetPiece != null) break
                        c += direction
                    }
                }
            }
            PieceType.BISHOP -> {
                for (rowDir in listOf(-1, 1)) {
                    for (colDir in listOf(-1, 1)) {
                        var r = piece.position.row + rowDir
                        var c = piece.position.col + colDir
                        while (isInBounds(r, c)) {
                            val targetPiece = board[r][c]
                            moves.add(Move(Position(r, c), targetPiece != null && targetPiece.color != piece.color))
                            if (targetPiece != null) break
                            r += rowDir
                            c += colDir
                        }
                    }
                }
            }
            PieceType.QUEEN -> {
                moves.addAll(calculateRawMoves(ChessPiece(PieceType.ROOK, piece.color, piece.position)))
                moves.addAll(calculateRawMoves(ChessPiece(PieceType.BISHOP, piece.color, piece.position)))
            }
            PieceType.KING -> {
                for (rowOffset in -1..1) {
                    for (colOffset in -1..1) {
                        if (rowOffset == 0 && colOffset == 0) continue
                        val newRow = piece.position.row + rowOffset
                        val newCol = piece.position.col + colOffset
                        if (isInBounds(newRow, newCol)) {
                            val targetPiece = board[newRow][newCol]
                            moves.add(Move(Position(newRow, newCol), targetPiece != null && targetPiece.color != piece.color))
                        }
                    }
                }
            }
        }
        return moves
    }

    private fun calculateCastlingMoves(piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE && piece.position == Position(0, 4) &&
                !hasMoved["white_king"]!! && !isKingInCheck(piece.color, piece.position)
            ) {
                if (!hasMoved["white_kingside_rook"]!! &&
                    board[0][5] == null && board[0][6] == null &&
                    !isSquareUnderAttack(piece.color, Position(0, 5)) &&
                    !isSquareUnderAttack(piece.color, Position(0, 6))
                ) {
                    moves.add(Move(Position(0, 6), false))
                }
                if (!hasMoved["white_queenside_rook"]!! &&
                    board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                    !isSquareUnderAttack(piece.color, Position(0, 2)) &&
                    !isSquareUnderAttack(piece.color, Position(0, 3))
                ) {
                    moves.add(Move(Position(0, 2), false))
                }
            }
            if (piece.color == PieceColor.BLACK && piece.position == Position(7, 4) &&
                !hasMoved["black_king"]!! && !isKingInCheck(piece.color, piece.position)
            ) {
                if (!hasMoved["black_kingside_rook"]!! &&
                    board[7][5] == null && board[7][6] == null &&
                    !isSquareUnderAttack(piece.color, Position(7, 5)) &&
                    !isSquareUnderAttack(piece.color, Position(7, 6))
                ) {
                    moves.add(Move(Position(7, 6), false))
                }
                if (!hasMoved["black_queenside_rook"]!! &&
                    board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                    !isSquareUnderAttack(piece.color, Position(7, 2)) &&
                    !isSquareUnderAttack(piece.color, Position(7, 3))
                ) {
                    moves.add(Move(Position(7, 2), false))
                }
            }
        }
        return moves
    }

    private fun isSquareUnderAttack(color: PieceColor, position: Position): Boolean {
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = calculateRawMoves(piece)
                    if (moves.any { it.position == position }) return true
                }
            }
        }
        return false
    }

    private fun hasKing(color: PieceColor): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return true
                }
            }
        }
        return false
    }

    private fun movePutsKingInCheck(piece: ChessPiece, to: Position): Boolean {
        if (!hasKing(piece.color)) return false

        val originalPiece = board[piece.position.row][piece.position.col]
        val targetPiece = board[to.row][to.col]
        board[to.row][to.col] = piece.copy(position = to)
        board[piece.position.row][piece.position.col] = null
        val kingPosition = findKing(piece.color)
        val inCheck = isKingInCheck(piece.color, kingPosition)
        board[piece.position.row][piece.position.col] = originalPiece
        board[to.row][to.col] = targetPiece
        return inCheck
    }

    private fun findKing(color: PieceColor): Position {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return Position(row, col)
                }
            }
        }
        isGameOver = true
        gameResult = "Game ended due to missing king for $color"
        return Position(-1, -1)
    }

    private fun isKingInCheck(color: PieceColor, kingPosition: Position): Boolean {
        if (kingPosition.row == -1 && kingPosition.col == -1) return false
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = calculateRawMoves(piece)
                    if (moves.any { it.position == kingPosition }) return true
                }
            }
        }
        return false
    }

    private fun isInBounds(row: Int, col: Int): Boolean = row in 0 until 8 && col in 0 until 8

    private fun checkGameState() {
        if (!hasKing(currentTurn)) {
            isGameOver = true
            gameResult = "Game ended due to missing king for $currentTurn"
            return
        }
        val kingPosition = findKing(currentTurn)
        if (isKingInCheck(currentTurn, kingPosition)) {
            if (isCheckmate()) {
                isGameOver = true
                gameResult = if (currentTurn == PieceColor.WHITE) "Black wins by checkmate!" else "White wins by checkmate!"
            }
        } else {
            if (isStalemate()) {
                isGameOver = true
                gameResult = "Stalemate! Game is a draw."
            }
        }
    }

    private fun isCheckmate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == currentTurn) {
                    val moves = calculateValidMoves(piece)
                    for (move in moves) {
                        if (!movePutsKingInCheck(piece, move.position)) return false
                    }
                }
            }
        }
        return true
    }

    private fun isStalemate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == currentTurn) {
                    val moves = calculateValidMoves(piece)
                    if (moves.isNotEmpty()) return false
                }
            }
        }
        return true
    }

    fun getBoard(): Array<Array<ChessPiece?>> = board
    fun getCurrentTurn(): PieceColor = currentTurn
    fun isGameOver(): Boolean = isGameOver
    fun getGameResult(): String? = gameResult
    fun getPendingPromotion(): Position? = pendingPromotion
}