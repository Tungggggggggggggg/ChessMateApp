package com.example.chessmate.model

/**
 * Lớp đại diện cho một ván cờ vua, quản lý trạng thái trò chơi, bàn cờ và các quy tắc.
 */
class ChessGame {
    // Bàn cờ 8x8, mỗi ô có thể chứa một ChessPiece hoặc null
    private var board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }

    // Theo dõi lượt đi hiện tại (TRẮNG hoặc ĐEN)
    private var currentTurn: PieceColor = PieceColor.WHITE

    // Quân cờ hiện đang được chọn để thực hiện nước đi
    private var selectedPiece: ChessPiece? = null

    // Danh sách các vị trí đích hợp lệ cho quân cờ được chọn
    private var validMoves: List<Position> = emptyList()

    // Cho biết ván cờ đã kết thúc hay chưa
    private var isGameOver: Boolean = false

    // Lưu trữ kết quả của ván cờ (ví dụ: "Trắng thắng", "Hòa")
    private var gameResult: String? = null

    // Lưu trữ nước đi cuối cùng dưới dạng cặp vị trí từ và đến
    private var lastMove: Pair<Position, Position>? = null

    // Theo dõi nếu một quân Tốt đang chờ được phong cấp (ví dụ: khi nó đến hàng cuối của đối thủ)
    private var pendingPromotion: Position? = null

    // Theo dõi các quân cờ đã di chuyển hay chưa (dùng cho nhập thành và các quy tắc khác)
    private val hasMoved: MutableMap<String, Boolean> = mutableMapOf(
        "white_king" to false,
        "white_kingside_rook" to false,
        "white_queenside_rook" to false,
        "black_king" to false,
        "black_kingside_rook" to false,
        "black_queenside_rook" to false
    )

    // Theo dõi vị trí của Vua trắng
    private var whiteKingPosition: Position = Position(0, 4)

    // Theo dõi vị trí của Vua đen
    private var blackKingPosition: Position = Position(7, 4)

    // Bộ đếm cho luật 50 nước (hòa nếu không có nước đi Tốt hoặc ăn quân trong 50 nước)
    private var fiftyMoveCounter: Int = 0

    // Theo dõi các trạng thái bàn cờ để phát hiện lặp lại ba lần cho điều kiện hòa
    private val positionHistory: MutableMap<String, Int> = mutableMapOf()

    /**
     * Khởi tạo ván cờ bằng cách thiết lập bàn cờ và lưu trạng thái ban đầu.
     */
    init {
        initializeBoard()
        saveBoardState()
    }

    /**
     * Trả về nước đi cuối cùng được thực hiện trong ván cờ.
     *
     * @return Cặp vị trí từ và đến, hoặc null nếu chưa có nước đi nào.
     */
    fun getLastMove(): Pair<Position, Position>? = lastMove

    /**
     * Thiết lập bàn cờ ban đầu với các quân cờ ở vị trí bắt đầu.
     */
    private fun initializeBoard() {
        // Đặt các quân cờ Trắng
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

        // Đặt các quân cờ Đen
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

    /**
     * Lấy quân cờ tại vị trí được chỉ định.
     *
     * @param row Chỉ số hàng (0 đến 7).
     * @param col Chỉ số cột (0 đến 7).
     * @return Quân cờ tại vị trí đó, hoặc null nếu vị trí trống.
     */
    fun getPieceAt(row: Int, col: Int): ChessPiece? = board[row][col]

    /**
     * Chọn một quân cờ tại vị trí được chỉ định và trả về danh sách các nước đi hợp lệ.
     *
     * @param row Chỉ số hàng của quân cờ.
     * @param col Chỉ số cột của quân cờ.
     * @return Danh sách các nước đi hợp lệ cho quân cờ được chọn.
     */
    fun selectPiece(row: Int, col: Int): List<Move> {
        val piece = board[row][col] ?: return emptyList()
        if (piece.color != currentTurn) return emptyList()
        selectedPiece = piece
        val moves = calculateValidMoves(piece)
        validMoves = moves.map { it.position }
        return moves
    }

    /**
     * Di chuyển quân cờ được chọn đến vị trí đích.
     *
     * @param to Vị trí đích của nước đi.
     * @return True nếu nước đi thành công, false nếu không hợp lệ.
     */
    fun movePiece(to: Position): Boolean {
        val piece = selectedPiece ?: return false
        val validMove = calculateValidMoves(piece).find { it.position == to } ?: return false
        val targetPiece = board[to.row][to.col]

        if (targetPiece != null && targetPiece.color == piece.color) return false

        val from = piece.position

        // Xử lý phong cấp cho Tốt
        if (piece.type == PieceType.PAWN && (to.row == 7 || to.row == 0)) {
            board[to.row][to.col] = piece.copy(position = to)
            board[from.row][from.col] = null
            pendingPromotion = to
            fiftyMoveCounter = 0
            saveBoardState()
            return true
        }

        // Kiểm tra nước đi bắt Tốt qua đường (en passant)
        val isEnPassant = piece.type == PieceType.PAWN && targetPiece == null &&
                to.col != piece.position.col && lastMove?.let { last ->
            last.second.row == piece.position.row &&
                    last.second.col == to.col &&
                    board[last.second.row][last.second.col]?.type == PieceType.PAWN &&
                    board[last.second.row][last.second.col]?.color != piece.color &&
                    kotlin.math.abs(last.first.row - last.second.row) == 2
        } == true

        // Kiểm tra nước đi nhập thành
        val isCastling = piece.type == PieceType.KING && kotlin.math.abs(to.col - piece.position.col) == 2
        val rookCol = if (to.col > piece.position.col) 7 else 0
        val rookNewCol = if (to.col > piece.position.col) 5 else 3
        val row = piece.position.row

        // Cập nhật bàn cờ
        board[to.row][to.col] = piece.copy(position = to)
        board[from.row][from.col] = null

        // Xử lý bắt Tốt qua đường
        if (isEnPassant) {
            lastMove?.let { last ->
                board[last.second.row][last.second.col] = null
            }
        }

        // Xử lý nhập thành
        if (isCastling) {
            val rook = board[row][rookCol]
            if (rook != null) {
                board[row][rookCol] = null
                board[row][rookNewCol] = rook.copy(position = Position(row, rookNewCol))
            }
        }

        // Cập nhật trạng thái di chuyển của Vua và Xe
        if (piece.type == PieceType.KING) {
            hasMoved["${piece.color}_king"] = true
            updateKingPosition(piece.color, to)
        }
        if (piece.type == PieceType.ROOK) {
            if (from.col == 0) hasMoved["${piece.color}_queenside_rook"] = true
            if (from.col == 7) hasMoved["${piece.color}_kingside_rook"] = true
        }

        // Cập nhật bộ đếm 50 nước
        if (piece.type == PieceType.PAWN || targetPiece != null || isEnPassant) {
            fiftyMoveCounter = 0
        } else {
            fiftyMoveCounter++
        }

        lastMove = Pair(from, to)
        currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        selectedPiece = null
        validMoves = emptyList()
        saveBoardState()
        checkGameState()
        return true
    }

    /**
     * Phong cấp quân Tốt thành một loại quân cờ khác.
     *
     * @param toType Loại quân cờ mà Tốt sẽ được phong cấp thành (Hậu, Xe, Tượng, Mã).
     */
    fun promotePawn(toType: PieceType) {
        pendingPromotion?.let { pos ->
            val piece = board[pos.row][pos.col]
            if (piece != null && piece.type == PieceType.PAWN) {
                board[pos.row][pos.col] = ChessPiece(toType, piece.color, pos)
            }
            pendingPromotion = null
            currentTurn = if (currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            saveBoardState()
            checkGameState()
        }
    }

    /**
     * Tính toán các nước đi hợp lệ cho một quân cờ.
     *
     * @param piece Quân cờ cần tính toán nước đi.
     * @return Danh sách các nước đi hợp lệ.
     */
    private fun calculateValidMoves(piece: ChessPiece): List<Move> {
        val rawMoves = calculateRawMoves(piece)
        val validMoves = rawMoves.filter { move ->
            val putsKingInCheck = movePutsKingInCheck(piece, move.position)
            if (piece.type == PieceType.KING) {
                val opponentKingPos = if (piece.color == PieceColor.WHITE) blackKingPosition else whiteKingPosition
                val isTooClose = kotlin.math.abs(move.position.row - opponentKingPos.row) <= 1 &&
                        kotlin.math.abs(move.position.col - opponentKingPos.col) <= 1
                !putsKingInCheck && !isTooClose
            } else {
                !putsKingInCheck
            }
        }
        if (piece.type == PieceType.KING) {
            val castlingMoves = calculateCastlingMoves(piece)
            return validMoves + castlingMoves
        }
        return validMoves
    }

    /**
     * Tính toán các nước đi thô (chưa kiểm tra hợp lệ) cho một quân cờ.
     *
     * @param piece Quân cờ cần tính toán.
     * @param forCheck Cho biết liệu tính toán có dành cho việc kiểm tra chiếu vua hay không.
     * @return Danh sách các nước đi thô.
     */
    private fun calculateRawMoves(piece: ChessPiece, forCheck: Boolean = false): List<Move> {
        val moves = mutableListOf<Move>()
        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) 1 else -1
                val startRow = if (piece.color == PieceColor.WHITE) 1 else 6
                val targetRow = piece.position.row + direction
                if (isInBounds(targetRow, piece.position.col) && board[targetRow][piece.position.col] == null) {
                    moves.add(Move(piece.position, Position(targetRow, piece.position.col), false))
                    if (piece.position.row == startRow && board[targetRow + direction][piece.position.col] == null) {
                        moves.add(Move(piece.position, Position(targetRow + direction, piece.position.col), false))
                    }
                }
                for (colOffset in listOf(-1, 1)) {
                    val newRow = piece.position.row + direction
                    val newCol = piece.position.col + colOffset
                    if (isInBounds(newRow, newCol)) {
                        val targetPiece = board[newRow][newCol]
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(Move(piece.position, Position(newRow, newCol), true))
                        } else if (targetPiece == null && lastMove?.let { last ->
                                last.second.row == piece.position.row &&
                                        last.second.col == newCol &&
                                        board[last.second.row][last.second.col]?.type == PieceType.PAWN &&
                                        board[last.second.row][last.second.col]?.color != piece.color &&
                                        kotlin.math.abs(last.first.row - last.second.row) == 2
                            } == true) {
                            moves.add(Move(piece.position, Position(newRow, newCol), true))
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
                        if (targetPiece == null || targetPiece.color != piece.color) {
                            moves.add(Move(piece.position, Position(newRow, newCol), targetPiece != null))
                        }
                    }
                }
            }
            PieceType.ROOK -> {
                for (direction in listOf(-1, 1)) {
                    var r = piece.position.row + direction
                    while (isInBounds(r, piece.position.col)) {
                        val targetPiece = board[r][piece.position.col]
                        if (targetPiece == null || targetPiece.color != piece.color) {
                            moves.add(Move(piece.position, Position(r, piece.position.col), targetPiece != null))
                        }
                        if (targetPiece != null) break
                        r += direction
                    }
                    var c = piece.position.col + direction
                    while (isInBounds(piece.position.row, c)) {
                        val targetPiece = board[piece.position.row][c]
                        if (targetPiece == null || targetPiece.color != piece.color) {
                            moves.add(Move(piece.position, Position(piece.position.row, c), targetPiece != null))
                        }
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
                            if (targetPiece == null || targetPiece.color != piece.color) {
                                moves.add(Move(piece.position, Position(r, c), targetPiece != null))
                            }
                            if (targetPiece != null) break
                            r += rowDir
                            c += colDir
                        }
                    }
                }
            }
            PieceType.QUEEN -> {
                moves.addAll(calculateRawMoves(ChessPiece(PieceType.ROOK, piece.color, piece.position), forCheck))
                moves.addAll(calculateRawMoves(ChessPiece(PieceType.BISHOP, piece.color, piece.position), forCheck))
            }
            PieceType.KING -> {
                for (rowOffset in -1..1) {
                    for (colOffset in -1..1) {
                        if (rowOffset == 0 && colOffset == 0) continue
                        val newRow = piece.position.row + rowOffset
                        val newCol = piece.position.col + colOffset
                        if (isInBounds(newRow, newCol)) {
                            val targetPiece = board[newRow][newCol]
                            if (targetPiece == null || targetPiece.color != piece.color) {
                                moves.add(Move(piece.position, Position(newRow, newCol), targetPiece != null))
                            }
                        }
                    }
                }
            }
        }
        return moves
    }

    /**
     * Tính toán các nước đi nhập thành cho Vua.
     *
     * @param piece Quân Vua cần kiểm tra.
     * @return Danh sách các nước đi nhập thành hợp lệ.
     */
    private fun calculateCastlingMoves(piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        if (piece.type == PieceType.KING && !isKingInCheck(piece.color)) {
            val row = piece.position.row
            if (piece.color == PieceColor.WHITE && row == 0 && !hasMoved["white_king"]!!) {
                if (!hasMoved["white_kingside_rook"]!! && board[0][5] == null && board[0][6] == null &&
                    !isSquareUnderAttack(piece.color, Position(0, 5)) &&
                    !isSquareUnderAttack(piece.color, Position(0, 6))
                ) {
                    moves.add(Move(piece.position, Position(0, 6), false))
                }
                if (!hasMoved["white_queenside_rook"]!! && board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                    !isSquareUnderAttack(piece.color, Position(0, 2)) &&
                    !isSquareUnderAttack(piece.color, Position(0, 3))
                ) {
                    moves.add(Move(piece.position, Position(0, 2), false))
                }
            }
            if (piece.color == PieceColor.BLACK && row == 7 && !hasMoved["black_king"]!!) {
                if (!hasMoved["black_kingside_rook"]!! && board[7][5] == null && board[7][6] == null &&
                    !isSquareUnderAttack(piece.color, Position(7, 5)) &&
                    !isSquareUnderAttack(piece.color, Position(7, 6))
                ) {
                    moves.add(Move(piece.position, Position(7, 6), false))
                }
                if (!hasMoved["black_queenside_rook"]!! && board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                    !isSquareUnderAttack(piece.color, Position(7, 2)) &&
                    !isSquareUnderAttack(piece.color, Position(7, 3))
                ) {
                    moves.add(Move(piece.position, Position(7, 2), false))
                }
            }
        }
        return moves
    }

    /**
     * Kiểm tra xem một ô có đang bị tấn công bởi quân đối thủ hay không.
     *
     * @param color Màu của người chơi cần kiểm tra.
     * @param position Vị trí ô cần kiểm tra.
     * @return True nếu ô bị tấn công, false nếu không.
     */
    private fun isSquareUnderAttack(color: PieceColor, position: Position): Boolean {
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = calculateRawMoves(piece, forCheck = true)
                    if (moves.any { it.position == position }) return true
                }
            }
        }
        return false
    }

    /**
     * Kiểm tra xem một nước đi có đặt Vua của bên mình vào thế bị chiếu hay không.
     *
     * @param piece Quân cờ được di chuyển.
     * @param to Vị trí đích của nước đi.
     * @return True nếu nước đi đặt Vua vào thế bị chiếu, false nếu không.
     */
    private fun movePutsKingInCheck(piece: ChessPiece, to: Position): Boolean {
        val from = piece.position
        val originalPiece = board[from.row][from.col]
        val targetPiece = board[to.row][to.col]
        val originalKingPos = if (piece.color == PieceColor.WHITE) whiteKingPosition else blackKingPosition

        board[to.row][to.col] = piece.copy(position = to)
        board[from.row][from.col] = null

        if (piece.type == PieceType.KING) {
            updateKingPosition(piece.color, to)
        }

        val inCheck = isKingInCheck(piece.color)

        board[from.row][from.col] = originalPiece
        board[to.row][to.col] = targetPiece
        if (piece.type == PieceType.KING) {
            updateKingPosition(piece.color, originalKingPos)
        }

        return inCheck
    }

    /**
     * Cập nhật vị trí của Vua.
     *
     * @param color Màu của Vua (TRẮNG hoặc ĐEN).
     * @param newPosition Vị trí mới của Vua.
     */
    private fun updateKingPosition(color: PieceColor, newPosition: Position) {
        if (color == PieceColor.WHITE) whiteKingPosition = newPosition
        else blackKingPosition = newPosition
    }

    /**
     * Kiểm tra xem Vua của một bên có đang bị chiếu hay không.
     *
     * @param color Màu của Vua cần kiểm tra.
     * @return True nếu Vua bị chiếu, false nếu không.
     */
    private fun isKingInCheck(color: PieceColor): Boolean {
        val kingPos = if (color == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = calculateRawMoves(piece, forCheck = true)
                    if (moves.any { it.position == kingPos }) return true
                }
            }
        }
        return false
    }

    /**
     * Kiểm tra xem một vị trí có nằm trong giới hạn bàn cờ hay không.
     *
     * @param row Chỉ số hàng.
     * @param col Chỉ số cột.
     * @return True nếu vị trí hợp lệ, false nếu không.
     */
    private fun isInBounds(row: Int, col: Int): Boolean = row in 0 until 8 && col in 0 until 8

    /**
     * Kiểm tra xem một ô có phải là ô sáng (light square) hay không.
     *
     * @param row Chỉ số hàng.
     * @param col Chỉ số cột.
     * @return True nếu là ô sáng, false nếu là ô tối.
     */
    private fun isLightSquare(row: Int, col: Int): Boolean {
        return (row + col) % 2 == 1
    }

    /**
     * Lưu trạng thái hiện tại của bàn cờ vào lịch sử để kiểm tra lặp lại.
     */
    private fun saveBoardState() {
        val state = getBoardStateHash()
        positionHistory[state] = positionHistory.getOrDefault(state, 0) + 1
    }

    /**
     * Tạo một chuỗi băm đại diện cho trạng thái hiện tại của bàn cờ.
     *
     * @return Chuỗi băm trạng thái bàn cờ.
     */
    private fun getBoardStateHash(): String {
        val sb = StringBuilder()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                sb.append(
                    if (piece == null) "0"
                    else "${piece.color}_${piece.type}_${piece.position.row}_${piece.position.col}"
                )
            }
        }
        sb.append("|").append(currentTurn)
        sb.append("|")
        hasMoved.forEach { (key, value) ->
            sb.append("$key:$value,")
        }
        sb.append("|")
        lastMove?.let {
            sb.append("${it.first.row},${it.first.col}-${it.second.row},${it.second.col}")
        } ?: sb.append("none")
        return sb.toString()
    }

    /**
     * Kiểm tra trạng thái ván cờ để xác định kết quả (thắng, hòa, hoặc tiếp tục).
     */
    private fun checkGameState() {
        val pieces = mutableListOf<ChessPiece>()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null) pieces.add(piece)
            }
        }

        val pieceCount = pieces.size
        if (pieceCount == 2) {
            if (pieces.all { it.type == PieceType.KING }) {
                isGameOver = true
                gameResult = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua vs Vua)."
                return
            }
        } else if (pieceCount == 3) {
            val kings = pieces.filter { it.type == PieceType.KING }
            val otherPiece = pieces.firstOrNull { it.type != PieceType.KING }
            if (kings.size == 2 && otherPiece != null) {
                if (otherPiece.type == PieceType.BISHOP || otherPiece.type == PieceType.KNIGHT) {
                    isGameOver = true
                    gameResult = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua và ${if (otherPiece.type == PieceType.BISHOP) "Tượng" else "Mã"} vs Vua)."
                    return
                }
            }
        } else if (pieceCount == 4) {
            val kings = pieces.filter { it.type == PieceType.KING }
            val bishops = pieces.filter { it.type == PieceType.BISHOP }
            if (kings.size == 2 && bishops.size == 2) {
                val bishop1 = bishops[0]
                val bishop2 = bishops[1]
                val bishop1IsLight = isLightSquare(bishop1.position.row, bishop1.position.col)
                val bishop2IsLight = isLightSquare(bishop2.position.row, bishop2.position.col)
                if (bishop1IsLight == bishop2IsLight) {
                    isGameOver = true
                    gameResult = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua và Tượng vs Vua và Tượng cùng màu ô)."
                    return
                }
            }
        }

        val currentState = getBoardStateHash()
        if (positionHistory[currentState] ?: 0 >= 3) {
            isGameOver = true
            gameResult = "Hết cờ! Ván đấu hòa (lặp lại vị trí 3 lần)."
            return
        }

        if (fiftyMoveCounter >= 50) {
            isGameOver = true
            gameResult = "Hết cờ! Ván đấu hòa (luật 50 nước: không có pawn move hoặc capture trong 50 nước đi)."
            return
        }

        val kingPos = if (currentTurn == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val king = board[kingPos.row][kingPos.col]
        if (king == null || king.type != PieceType.KING || king.color != currentTurn) {
            var kingFound = false
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val piece = board[row][col]
                    if (piece != null && piece.type == PieceType.KING && piece.color == currentTurn) {
                        kingFound = true
                        if (currentTurn == PieceColor.WHITE) whiteKingPosition = piece.position
                        else blackKingPosition = piece.position
                        break
                    }
                }
                if (kingFound) break
            }
            if (!kingFound) {
                isGameOver = true
                gameResult = "Ván cờ kết thúc do thiếu Vua của $currentTurn"
                return
            }
        }

        if (isKingInCheck(currentTurn)) {
            if (isCheckmate()) {
                isGameOver = true
                gameResult = if (currentTurn == PieceColor.WHITE) "Đen thắng vì đã chiếu hết!" else "Trắng thắng vì đã chiếu hết!"
            }
        } else if (isStalemate()) {
            isGameOver = true
            gameResult = "Hết cờ! Ván đấu hòa (thế cờ chết)."
        }
    }

    /**
     * Hàm công khai để gọi kiểm tra trạng thái ván cờ từ bên ngoài.
     */
    fun updateGameState() {
        checkGameState()
    }

    /**
     * Kiểm tra xem có phải là thế chiếu hết hay không.
     *
     * @return True nếu là chiếu hết, false nếu không.
     */
    private fun isCheckmate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == currentTurn) {
                    val moves = calculateValidMoves(piece)
                    if (moves.any { !movePutsKingInCheck(piece, it.position) }) return false
                }
            }
        }
        return true
    }

    /**
     * Kiểm tra xem có phải là thế cờ chết (stalemate) hay không.
     *
     * @return True nếu là thế cờ chết, false nếu không.
     */
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

    // Các getter và setter để đồng bộ với OnlineChessViewModel
    /**
     * Lấy trạng thái hiện tại của bàn cờ.
     */
    fun getBoard(): Array<Array<ChessPiece?>> = board

    /**
     * Lấy lượt đi hiện tại.
     */
    fun getCurrentTurn(): PieceColor = currentTurn

    /**
     * Kiểm tra xem ván cờ đã kết thúc hay chưa.
     */
    fun isGameOver(): Boolean = isGameOver

    /**
     * Lấy kết quả của ván cờ.
     */
    fun getGameResult(): String? = gameResult

    /**
     * Lấy vị trí của quân Tốt đang chờ phong cấp.
     */
    fun getPendingPromotion(): Position? = pendingPromotion

    /**
     * Lấy giá trị bộ đếm 50 nước.
     */
    fun getFiftyMoveCounter(): Int = fiftyMoveCounter

    /**
     * Thiết lập giá trị bộ đếm 50 nước.
     */
    fun setFiftyMoveCounter(value: Int) {
        fiftyMoveCounter = value
    }

    /**
     * Lấy danh sách các trạng thái bàn cờ đã được lưu.
     */
    fun getPositionHistory(): List<String> = positionHistory.keys.toList()

    /**
     * Thiết lập lịch sử trạng thái bàn cờ.
     */
    fun setPositionHistory(history: List<String>) {
        positionHistory.clear()
        history.forEach { pos ->
            positionHistory[pos] = positionHistory[pos]?.plus(1) ?: 1
        }
    }

    /**
     * Thiết lập lượt đi hiện tại.
     */
    fun setCurrentTurn(turn: PieceColor) {
        currentTurn = turn
    }

    /**
     * Lấy vị trí của Vua trắng.
     */
    fun getWhiteKingPosition(): Position = whiteKingPosition

    /**
     * Lấy vị trí của Vua đen.
     */
    fun getBlackKingPosition(): Position = blackKingPosition

    /**
     * Lấy trạng thái di chuyển của các quân cờ.
     */
    fun getHasMoved(): Map<String, Boolean> = hasMoved

    /**
     * Thiết lập vị trí của Vua trắng.
     */
    fun setWhiteKingPosition(position: Position) {
        whiteKingPosition = position
    }

    /**
     * Thiết lập vị trí của Vua đen.
     */
    fun setBlackKingPosition(position: Position) {
        blackKingPosition = position
    }

    /**
     * Thiết lập trạng thái di chuyển của một quân cờ.
     */
    fun setHasMoved(key: String, value: Boolean) {
        hasMoved[key] = value
    }

    /**
     * Thiết lập nước đi cuối cùng.
     */
    fun setLastMove(from: Position?, to: Position?) {
        lastMove = if (from != null && to != null) Pair(from, to) else null
    }

    // Các phương thức công khai để truy cập logic riêng
    /**
     * Kiểm tra xem Vua có đang bị chiếu hay không (công khai).
     */
    fun isKingInCheckPublic(color: PieceColor): Boolean {
        return isKingInCheck(color)
    }

    /**
     * Kiểm tra xem có phải là chiếu hết hay không (công khai).
     */
    fun isCheckmatePublic(): Boolean {
        return isCheckmate()
    }

    /**
     * Kiểm tra xem có phải là thế cờ chết hay không (công khai).
     */
    fun isStalematePublic(): Boolean {
        return isStalemate()
    }

    /**
     * Kiểm tra xem ván cờ có hòa theo luật 50 nước hay không (công khai).
     */
    fun isDrawByFiftyMoveRulePublic(): Boolean {
        return fiftyMoveCounter >= 50
    }

    /**
     * Kiểm tra xem ván cờ có hòa do lặp lại vị trí hay không (công khai).
     */
    fun isDrawByRepetitionPublic(): Boolean {
        return positionHistory.values.any { it >= 3 }
    }
}