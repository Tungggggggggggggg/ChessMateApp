package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.chessmate.model.ChatMessage
import com.example.chessmate.viewmodel.ChatViewModel
import com.example.chessmate.viewmodel.ChatViewModelFactory
import android.util.Log
import kotlinx.coroutines.delay

@Composable
fun ChatDetailHeader(
    friendName: String,
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
        Spacer(modifier = Modifier.width(12.dp))
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = friendName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isCurrentUser) Color(0xFFBBDEFB) else Color.White,
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                        bottomEnd = if (isCurrentUser) 0.dp else 12.dp
                    )
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ChatDetailContent(
    messages: List<ChatMessage>,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        val listState = rememberLazyListState()
        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                delay(100)
                listState.animateScrollToItem(0) // Cuộn đến mục đầu tiên (dưới cùng do reverseLayout)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            reverseLayout = true, // Đảo ngược để tin nhắn mới ở dưới
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages.reversed()) { message -> // Đảo ngược danh sách để khớp với reverseLayout
                MessageItem(
                    message = message,
                    isCurrentUser = message.senderId == currentUserId
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.image),
                contentDescription = "Hình ảnh",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.mic),
                contentDescription = "Micro",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Nhập tin nhắn...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSendMessage) {
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Gửi",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    navController: NavController? = null,
    friendId: String,
    friendName: String,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory()),
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val messages by viewModel.messages.collectAsState()
    val hasUnreadMessages by viewModel.hasUnreadMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(friendId) {
        val currentUserId = viewModel.currentUserId ?: ""
        val conversationId = viewModel.getConversationId(currentUserId, friendId)
        Log.d("ChatDetailScreen", "Friend ID: $friendId, Conversation ID: $conversationId")
        viewModel.listenToChatMessages(friendId)
        if (hasUnreadMessages) {
            viewModel.markMessagesAsRead(friendId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatDetailHeader(
            friendName = friendName,
            onBackClick = onBackClick
        )
        ChatDetailContent(
            messages = messages,
            currentUserId = viewModel.currentUserId,
            modifier = Modifier.weight(1f)
        )
        ChatInput(
            messageText = messageText,
            onMessageChange = { messageText = it },
            onSendMessage = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(friendId, messageText)
                    messageText = ""
                }
            }
        )
    }
}