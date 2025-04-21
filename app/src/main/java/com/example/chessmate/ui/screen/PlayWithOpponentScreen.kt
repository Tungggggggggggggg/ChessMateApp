package com.example.chessmate.ui.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.ChatMessage
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PromotionDialog
import com.example.chessmate.viewmodel.FindFriendsViewModel
import com.example.chessmate.viewmodel.OnlineChessViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Giao diện header cho màn hình chơi cờ trực tuyến, hiển thị thông tin đối thủ, điểm, thời gian và nút kết bạn.
 *
 * @param onExitConfirm Hàm xử lý khi người dùng xác nhận thoát trận đấu.
 * @param opponentName Tên của đối thủ.
 * @param opponentScore Điểm của đối thủ.
 * @param whiteTime Thời gian còn lại của bên trắng (tính bằng giây).
 * @param blackTime Thời gian còn lại của bên đen (tính bằng giây).
 * @param playerColor Màu quân cờ của người chơi (trắng hoặc đen).
 * @param opponentId ID của đối thủ.
 * @param friendViewModel ViewModel để quản lý yêu cầu kết bạn.
 * @param onProfileClick Hàm xử lý khi nhấn vào ảnh hồ sơ của đối thủ.
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithOpponentHeader(
    onExitConfirm: () -> Unit,
    opponentName: String,
    opponentScore: Int,
    whiteTime: Int,
    blackTime: Int,
    playerColor: PieceColor?,
    opponentId: String?,
    friendViewModel: FindFriendsViewModel,
    onProfileClick: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    // Trạng thái hiển thị dialog xác nhận thoát
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Theo dõi trạng thái yêu cầu kết bạn và danh sách bạn bè
    val sentRequests by friendViewModel.sentRequests.collectAsState()
    val friends by friendViewModel.friends.collectAsState()
    // Kiểm tra xem đã gửi yêu cầu kết bạn hoặc đã là bạn bè
    val isFriendRequestSent = opponentId?.let { it in sentRequests } ?: false
    val isFriend = opponentId?.let { id -> friends.any { it.userId == id } } ?: false

    // Box chứa toàn bộ header
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(120.dp)
    ) {
        // Nút thoát trận đấu (hình chữ X)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = CircleShape)
                .clickable { showExitDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "X",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Thông tin đối thủ (ảnh, tên)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh hồ sơ đối thủ, có thể nhấn để xem chi tiết
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = opponentName,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onProfileClick() },
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = opponentName,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Điểm và thời gian của đối thủ
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-106).dp, y = 10.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Điểm: $opponentScore",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Hiển thị thời gian của đối thủ (dựa trên màu quân cờ)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .offset(x = 8.dp)
                    .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(if (playerColor == PieceColor.BLACK) whiteTime else blackTime),
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Nút kết bạn, thay đổi trạng thái dựa trên quan hệ bạn bè
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 106.dp)
                .padding(top = 24.dp)
                .width(100.dp)
                .height(32.dp)
                .background(
                    colorResource(id = if (isFriendRequestSent || isFriend) R.color.color_b36a5e else R.color.color_eed7c5),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(enabled = !isFriend) {
                    opponentId?.let { id ->
                        if (!isFriend) {
                            if (isFriendRequestSent) {
                                friendViewModel.cancelFriendRequest(id)
                                Toast.makeText(context, "Đã hủy lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                            } else {
                                friendViewModel.sendFriendRequest(id)
                                Toast.makeText(context, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    isFriend -> "Bạn bè"
                    isFriendRequestSent -> "Xóa lời mời"
                    else -> "Kết bạn"
                },
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog xác nhận thoát trận đấu
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Thoát trận đấu",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn thoát trận đấu không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onExitConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}

/**
 * Giao diện footer cho màn hình chơi cờ trực tuyến, hiển thị thông tin người chơi, thời gian và các nút hành động.
 *
 * @param onOfferDraw Hàm xử lý khi nhấn nút cầu hòa.
 * @param onSurrender Hàm xử lý khi nhấn nút đầu hàng.
 * @param playerName Tên của người chơi.
 * @param playerScore Điểm của người chơi.
 * @param whiteTime Thời gian còn lại của bên trắng (tính bằng giây).
 * @param blackTime Thời gian còn lại của bên đen (tính bằng giây).
 * @param playerColor Màu quân cờ của người chơi (trắng hoặc đen).
 * @param hasUnreadMessages Trạng thái có tin nhắn chưa đọc hay không.
 * @param onChatClick Hàm xử lý khi nhấn nút chat.
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithOpponentFooter(
    onOfferDraw: () -> Unit,
    onSurrender: () -> Unit,
    playerName: String,
    playerScore: Int,
    whiteTime: Int,
    blackTime: Int,
    playerColor: PieceColor?,
    hasUnreadMessages: Boolean,
    onChatClick: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    // Trạng thái hiển thị dialog xác nhận cầu hòa và đầu hàng
    var showDrawDialog by remember { mutableStateOf(false) }
    var showSurrenderDialog by remember { mutableStateOf(false) }

    // Box chứa toàn bộ footer
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(108.dp)
    ) {
        // Nhóm bên trái: Tên và điểm
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh hồ sơ người chơi
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = playerName,
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp)) // Khoảng cách giữa biểu tượng và tên
            Column {
                // Tên người chơi
                Text(
                    text = playerName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp)) // Khoảng cách giữa tên và điểm
                // Điểm của người chơi
                Text(
                    text = "Điểm: $playerScore",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Nhóm bên phải: Column chứa hai Row
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa hai Row
        ) {
            // Row 1: Nút tin nhắn và thời gian
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Khoảng cách giữa các nút trong Row
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút tin nhắn
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                        .clickable { onChatClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.message),
                        contentDescription = "Message",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                    // Hiển thị chấm đỏ nếu có tin nhắn chưa đọc
                    if (hasUnreadMessages) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                }
                // Thời gian của người chơi
                Box(
                    modifier = Modifier
                        .width(82.dp)
                        .height(32.dp)
                        .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatTime(if (playerColor == PieceColor.WHITE) whiteTime else blackTime),
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Row 2: Nút cầu hòa và đầu hàng
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Khoảng cách giữa các nút trong Row
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút cầu hòa
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp)
                        .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                        .clickable { showDrawDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cầu hòa",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Nút đầu hàng
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(32.dp)
                        .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                        .clickable { showSurrenderDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đầu hàng",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Dialog xác nhận cầu hòa
    if (showDrawDialog) {
        AlertDialog(
            onDismissRequest = { showDrawDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Cầu hòa",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn gửi yêu cầu cầu hòa không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDrawDialog = false
                        onOfferDraw()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDrawDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    // Dialog xác nhận đầu hàng
    if (showSurrenderDialog) {
        AlertDialog(
            onDismissRequest = { showSurrenderDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Đầu hàng",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đầu hàng không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSurrenderDialog = false
                        onSurrender()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSurrenderDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}

/**
 * Định dạng thời gian từ giây sang định dạng phút:giây (MM:SS).
 *
 * @param seconds Số giây cần định dạng.
 * @return Chuỗi thời gian định dạng MM:SS.
 */
@SuppressLint("DefaultLocale")
@Composable
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

/**
 * Dialog hiển thị khung chat giữa hai người chơi trong trận đấu.
 *
 * @param messages Danh sách tin nhắn trong cuộc trò chuyện.
 * @param currentUserId ID của người dùng hiện tại.
 * @param opponentId ID của đối thủ.
 * @param onSendMessage Hàm xử lý khi gửi tin nhắn mới.
 * @param onDismiss Hàm xử lý khi đóng dialog.
 */
@Composable
fun ChatDialog(
    messages: List<ChatMessage>,
    currentUserId: String?,
    opponentId: String?,
    onSendMessage: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    // Trạng thái nhập tin nhắn
    var messageInput by remember { mutableStateOf("") }
    // Trạng thái cuộn của danh sách tin nhắn
    val listState = rememberLazyListState()

    // Tự động cuộn đến tin nhắn mới nhất khi danh sách thay đổi
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Dialog chứa khung chat
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(colorResource(id = R.color.color_c97c5d))
            .height(400.dp),
        title = {
            Text(
                text = "Chat",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Danh sách tin nhắn cuộn được
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(messages) { _, message ->
                        val isCurrentUser = message.senderId == currentUserId
                        // Hiển thị tin nhắn, căn chỉnh dựa trên người gửi
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        colorResource(
                                            id = if (isCurrentUser) R.color.color_eed7c5 else R.color.color_c89f9c
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = message.message,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                // Khu vực nhập tin nhắn và nút gửi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        placeholder = { Text("Nhập tin nhắn...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Nút gửi tin nhắn
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank() && opponentId != null) {
                                onSendMessage(opponentId, messageInput)
                                messageInput = ""
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Gửi",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color.White)
            }
        },
        containerColor = colorResource(id = R.color.color_c97c5d)
    )
}

/**
 * Màn hình chính để chơi cờ trực tuyến với đối thủ, hiển thị bàn cờ, header, footer và các dialog liên quan.
 *
 * @param navController Điều hướng để quay lại màn hình trước đó.
 * @param matchId ID của trận đấu.
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param chessViewModel ViewModel quản lý logic trò chơi trực tuyến.
 * @param friendViewModel ViewModel quản lý chức năng kết bạn.
 */
@Composable
fun PlayWithOpponentScreen(
    navController: NavController? = null,
    matchId: String = "",
    onBackClick: () -> Unit = { navController?.popBackStack() },
    chessViewModel: OnlineChessViewModel = viewModel(),
    friendViewModel: FindFriendsViewModel = viewModel()
) {
    // Khởi tạo các trạng thái hiển thị dialog và thông tin người chơi
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showDrawRequestDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("Bạn") }
    var opponentName by remember { mutableStateOf("Đối thủ") }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var opponentId by remember { mutableStateOf<String?>(null) }
    LocalContext.current
    // ID của người dùng hiện tại
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    // Danh sách tin nhắn và trạng thái tin nhắn chưa đọc
    val messages = chessViewModel.chatMessages // Directly use SnapshotStateList
    val hasUnreadMessages by chessViewModel.hasUnreadMessages

    // Lắng nghe tin nhắn khi có opponentId
    LaunchedEffect(opponentId) {
        opponentId?.let {
            chessViewModel.listenToChatMessages()
        }
    }

    // Đánh dấu tin nhắn đã đọc khi mở dialog
    LaunchedEffect(showChatDialog, opponentId) {
        if (showChatDialog) {
            opponentId?.let {
                chessViewModel.markMessagesAsRead()
            }
        }
    }

    // Tải trạng thái bạn bè và lời mời khi màn hình được tạo
    LaunchedEffect(Unit) {
        friendViewModel.loadSentRequests()
        friendViewModel.loadFriends()
    }

    // Cập nhật matchId và lắng nghe cập nhật trận đấu
    LaunchedEffect(matchId) {
        if (chessViewModel.matchId.value != matchId) {
            chessViewModel.matchId.value = matchId
            chessViewModel.listenToMatchUpdates()
        }
    }

    // Lắng nghe thông tin trận đấu và người chơi từ Firestore
    LaunchedEffect(chessViewModel.matchId.value) {
        chessViewModel.matchId.value?.let { id ->
            val db = Firebase.firestore
            db.collection("matches").document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val player1Id = snapshot.getString("player1")
                    val player2Id = snapshot.getString("player2")
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                    if (player1Id != null && player2Id != null && currentUserId != null) {
                        // Xác định màu quân cờ của người chơi
                        val expectedColor = if (currentUserId == player1Id) PieceColor.WHITE else PieceColor.BLACK
                        if (chessViewModel.playerColor.value != expectedColor) {
                            chessViewModel.playerColor.value = expectedColor
                        }

                        // Xác định ID của đối thủ
                        opponentId = if (currentUserId == player1Id) player2Id else player1Id

                        // Lấy thông tin người chơi 1
                        db.collection("users").document(player1Id)
                            .addSnapshotListener { player1Doc, player1Error ->
                                if (player1Error != null || player1Doc == null) return@addSnapshotListener
                                val player1Name = player1Doc.getString("name") ?: player1Doc.getString("username") ?: "Người chơi 1"
                                val player1Score = player1Doc.getLong("score")?.toInt() ?: 0

                                // Lấy thông tin người chơi 2
                                db.collection("users").document(player2Id)
                                    .addSnapshotListener { player2Doc, player2Error ->
                                        if (player2Error != null || player2Doc == null) return@addSnapshotListener
                                        val player2Name = player2Doc.getString("name") ?: player2Doc.getString("username") ?: "Người chơi 2"
                                        val player2Score = player2Doc.getLong("score")?.toInt() ?: 0

                                        // Cập nhật thông tin người chơi và đối thủ
                                        if (currentUserId == player1Id) {
                                            playerName = player1Name
                                            playerScore = player1Score
                                            opponentName = player2Name
                                            opponentScore = player2Score
                                        } else {
                                            playerName = player2Name
                                            playerScore = player2Score
                                            opponentName = player1Name
                                            opponentScore = player1Score
                                        }
                                    }
                            }
                    }
                }
        }
    }

    // Giao diện chính sử dụng Scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header hiển thị thông tin đối thủ
            PlayWithOpponentHeader(
                onExitConfirm = {
                    chessViewModel.surrender()
                    onBackClick()
                },
                opponentName = opponentName,
                opponentScore = opponentScore,
                whiteTime = chessViewModel.whiteTime.value,
                blackTime = chessViewModel.blackTime.value,
                playerColor = chessViewModel.playerColor.value,
                opponentId = opponentId,
                friendViewModel = friendViewModel,
                onProfileClick = {
                    opponentId?.let { id ->
                        navController?.navigate("competitor_profile/$id")
                    }
                }
            )
            // Hiển thị màu quân cờ của người chơi
            Text(
                text = "Bạn là bên ${if (chessViewModel.playerColor.value == PieceColor.WHITE) "trắng" else "đen"}",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            // Bàn cờ chính
            Chessboard(
                board = chessViewModel.board.value,
                highlightedSquares = chessViewModel.highlightedSquares.value,
                onSquareClicked = { row, col -> chessViewModel.onSquareClicked(row, col) },
                playerColor = chessViewModel.playerColor.value,
                clickable = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            // Hiển thị nước đi gần nhất
            Text(
                text = chessViewModel.moveHistory.lastOrNull() ?: "Chưa có nước đi",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            // Footer hiển thị thông tin người chơi
            PlayWithOpponentFooter(
                onOfferDraw = { chessViewModel.requestDraw() },
                onSurrender = {
                    chessViewModel.surrender()
                    showGameOverDialog = true
                },
                playerName = playerName,
                playerScore = playerScore,
                whiteTime = chessViewModel.whiteTime.value,
                blackTime = chessViewModel.blackTime.value,
                playerColor = chessViewModel.playerColor.value,
                hasUnreadMessages = hasUnreadMessages,
                onChatClick = { showChatDialog = true }
            )
        }
    }

    // Dialog hiển thị khi cần chọn quân để phong cấp
    if (chessViewModel.isPromoting.value) {
        PromotionDialog(
            playerColor = chessViewModel.playerColor.value ?: PieceColor.WHITE,
            onSelect = { pieceType ->
                chessViewModel.promotePawn(pieceType)
            },
            onDismiss = {}
        )
    }

    // Dialog hiển thị khi đối thủ gửi yêu cầu cầu hòa
    if (chessViewModel.drawRequest.value != null && chessViewModel.drawRequest.value != FirebaseAuth.getInstance().currentUser?.uid) {
        showDrawRequestDialog = true
    }

    if (showDrawRequestDialog) {
        AlertDialog(
            onDismissRequest = { showDrawRequestDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Yêu cầu cầu hòa",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Đối thủ đã gửi yêu cầu cầu hòa. Bạn có đồng ý không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDrawRequestDialog = false
                        chessViewModel.acceptDraw()
                        showGameOverDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDrawRequestDialog = false
                    chessViewModel.declineDraw()
                }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    // Dialog thông báo khi trò chơi kết thúc
    if (chessViewModel.isGameOver.value || showGameOverDialog) {
        AlertDialog(
            onDismissRequest = {},
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Ván đấu kết thúc",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = chessViewModel.gameResult.value ?: "Trò chơi kết thúc.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGameOverDialog = false
                        if (navController != null) {
                            navController.popBackStack()
                        } else {
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {},
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    // Dialog hiển thị khung chat
    if (showChatDialog) {
        ChatDialog(
            messages = messages, // Directly pass SnapshotStateList
            currentUserId = currentUserId,
            opponentId = opponentId,
            onSendMessage = { _, message ->
                chessViewModel.sendMessage(message)
            },
            onDismiss = { showChatDialog = false }
        )
    }
}