package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.User
import com.example.chessmate.viewmodel.ChatViewModel
import com.example.chessmate.viewmodel.ChatViewModelFactory
import java.net.URLEncoder
import android.util.Log

@Composable
fun ChatScreen(
    navController: NavController? = null,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory()),
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val friends by viewModel.friends.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf<Pair<User, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        ChatHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (friends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có bạn bè nào.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            } else {
                friends.forEach { friend ->
                    FriendListItem(
                        friend = friend,
                        onRemoveFriend = { showConfirmationDialog = Pair(friend, true) },
                        onClick = {
                            Log.d("ChatScreen", "Navigating to friend: ID=${friend.userId}, Name=${friend.name}")
                            val encodedFriendName = URLEncoder.encode(friend.name, "UTF-8")
                            navController?.navigate("chat_detail/${friend.userId}/$encodedFriendName")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        showConfirmationDialog?.let { (friend, show) ->
            if (show) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = null },
                    title = {
                        Text(
                            text = "Xóa bạn bè",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text("Bạn có chắc chắn muốn xóa '${friend.name}' khỏi danh sách bạn bè không?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.removeFriend(friend)
                            showConfirmationDialog = null
                        }) {
                            Text("Xóa", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmationDialog = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
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

        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = "Bạn bè",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
fun FriendListItem(
    friend: User,
    onRemoveFriend: (User) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black)
            .background(colorResource(id = R.color.color_c97c5d))
            .padding(12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = friend.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onRemoveFriend(friend) }) {
            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Xóa bạn bè",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}