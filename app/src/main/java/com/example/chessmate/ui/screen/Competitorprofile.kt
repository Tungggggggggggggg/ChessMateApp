package com.example.chessmate.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme
import com.example.chessmate.viewmodel.FindFriendsViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Hiển thị tiêu đề của màn hình hồ sơ đối thủ.
 *
 * @param onBackClick Hàm xử lý khi nhấn nút quay lại.
 * @param modifier Modifier tùy chỉnh.
 */
@Composable
fun CompetitorProfileHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Hồ sơ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(44.dp))
    }
}

/**
 * Hiển thị nội dung hồ sơ của đối thủ.
 *
 * @param modifier Modifier tùy chỉnh.
 * @param userData Dữ liệu người dùng từ Firestore.
 * @param onMatchHistoryClick Hàm xử lý khi nhấn nút lịch sử trận đấu.
 * @param onAddFriendClick Hàm xử lý khi nhấn nút kết bạn.
 * @param onCancelFriendRequestClick Hàm xử lý khi hủy lời mời kết bạn.
 * @param onRemoveFriendClick Hàm xử lý khi xóa bạn.
 * @param isFriendRequestSent True nếu đã gửi lời mời kết bạn.
 * @param isFriend True nếu đã là bạn bè.
 */
@Composable
fun CompetitorProfileContent(
    modifier: Modifier = Modifier,
    userData: Map<String, Any>? = null,
    onMatchHistoryClick: () -> Unit = {},
    onAddFriendClick: () -> Unit = {},
    onCancelFriendRequestClick: () -> Unit = {},
    onRemoveFriendClick: () -> Unit = {},
    isFriendRequestSent: Boolean = false,
    isFriend: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c97c5d)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Ảnh đại diện đối thủ",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onMatchHistoryClick,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Lịch sử trận đấu",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    when {
                        isFriend -> onRemoveFriendClick()
                        isFriendRequestSent -> onCancelFriendRequestClick()
                        else -> onAddFriendClick()
                    }
                },
                enabled = true,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isFriend -> Color.Red
                        isFriendRequestSent -> colorResource(id = R.color.color_b36a5e)
                        else -> colorResource(id = R.color.color_c89f9c)
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when {
                        isFriend -> "Xóa bạn"
                        isFriendRequestSent -> "Xóa lời mời"
                        else -> "Kết bạn"
                    },
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .border(1.dp, Color.Black)
                .background(colorResource(id = R.color.color_eee2df))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileInfoRow(label = "Tên:", value = userData?.get("name")?.toString() ?: "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Email:", value = userData?.get("email")?.toString() ?: "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Ngày tạo:", value = userData?.get("createdAt")?.toString() ?: "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Điểm:", value = userData?.get("score")?.toString() ?: "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Mô tả:", value = userData?.get("description")?.toString() ?: "")
        }
    }
}

/**
 * Màn hình hiển thị hồ sơ của đối thủ.
 *
 * @param navController Điều hướng đến các màn hình khác.
 * @param opponentId ID của đối thủ.
 * @param onBackClick Hàm xử lý khi nhấn nút quay lại.
 * @param onMatchHistoryClick Hàm xử lý khi nhấn nút lịch sử trận đấu.
 * @param friendViewModel ViewModel quản lý danh sách bạn bè.
 */
@Composable
fun CompetitorProfileScreen(
    navController: NavController? = null,
    opponentId: String = "",
    onBackClick: () -> Unit = { navController?.popBackStack() },
    onMatchHistoryClick: () -> Unit = {
        if (opponentId.isNotEmpty()) {
            navController?.navigate("match_history/$opponentId")
        }
    },
    friendViewModel: FindFriendsViewModel = viewModel()
) {
    val context = LocalContext.current
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val sentRequests by friendViewModel.sentRequests.collectAsState()
    val friends by friendViewModel.friends.collectAsState()
    val isFriendRequestSent = opponentId in sentRequests
    val isFriend = friends.any { it.userId == opponentId }
    var showRemoveFriendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(opponentId) {
        if (opponentId.isNotEmpty()) {
            Firebase.firestore.collection("users")
                .document(opponentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.data
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Lỗi khi tải thông tin đối thủ!", Toast.LENGTH_SHORT).show()
                }

            friendViewModel.loadSentRequests()
            friendViewModel.loadFriends()
        }
    }

    if (showRemoveFriendDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveFriendDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Xác nhận xóa bạn bè",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa ${userData?.get("name")?.toString() ?: "người này"} khỏi danh sách bạn bè không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        friendViewModel.removeFriend(opponentId)
                        Toast.makeText(context, "Đã xóa bạn bè!", Toast.LENGTH_SHORT).show()
                        showRemoveFriendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "Xác nhận",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveFriendDialog = false }
                ) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CompetitorProfileHeader(onBackClick = onBackClick)
        CompetitorProfileContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userData = userData,
            onMatchHistoryClick = onMatchHistoryClick,
            onAddFriendClick = {
                if (!isFriendRequestSent && !isFriend) {
                    friendViewModel.sendFriendRequest(opponentId)
                    Toast.makeText(context, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                }
            },
            onCancelFriendRequestClick = {
                friendViewModel.cancelFriendRequest(opponentId)
                Toast.makeText(context, "Đã hủy lời mời kết bạn!", Toast.LENGTH_SHORT).show()
            },
            onRemoveFriendClick = {
                showRemoveFriendDialog = true
            },
            isFriendRequestSent = isFriendRequestSent,
            isFriend = isFriend
        )
    }
}