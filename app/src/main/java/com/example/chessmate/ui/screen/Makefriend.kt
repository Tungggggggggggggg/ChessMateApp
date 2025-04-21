package com.example.chessmate.ui.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.model.FriendRequest
import com.example.chessmate.model.User
import com.example.chessmate.viewmodel.FindFriendsViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.graphicsLayer
import com.example.chessmate.viewmodel.ChatViewModel

/**
 * Hiển thị nút quay lại.
 *
 * @param onBackClick Hàm xử lý khi nhấn nút quay lại.
 * @param modifier Modifier tùy chỉnh.
 */
@Composable
fun BackButton(
    onBackClick: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Hiển thị thanh tìm kiếm người dùng.
 *
 * @param text Nội dung tìm kiếm.
 * @param onTextChanged Hàm xử lý khi nội dung thay đổi.
 * @param onSearch Hàm xử lý khi nhấn tìm kiếm.
 * @param modifier Modifier tùy chỉnh.
 */
@Composable
fun SearchBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp)
            .background(
                colorResource(id = R.color.color_c89f9c),
                shape = RoundedCornerShape(25.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        onSearch()
                        true
                    } else {
                        false
                    }
                },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "Nhập tên người chơi ...",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
        )
        IconButton(onClick = onSearch) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Tìm kiếm",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Màn hình tìm kiếm và quản lý bạn bè.
 *
 * @param navController Điều hướng đến các màn hình khác.
 * @param viewModel ViewModel quản lý logic tìm kiếm và bạn bè.
 * @param chatViewModel ViewModel quản lý tin nhắn.
 */
@Composable
fun FindFriendsScreen(
    navController: NavController,
    viewModel: FindFriendsViewModel,
    chatViewModel: ChatViewModel
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val receivedRequests by viewModel.receivedRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val friends by viewModel.friends.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var currentSearchQuery by remember { mutableStateOf("") }
    var isSearchEmpty by remember { mutableStateOf(false) }
    var isSearchResultsExpanded by remember { mutableStateOf(true) }
    var isFriendRequestsExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadReceivedRequests()
        viewModel.loadSentRequests()
        viewModel.loadFriends()
    }

    val sortedSearchResults = searchResults.sortedWith(compareBy {
        when {
            friends.any { friend -> friend.userId == it.userId } -> 0
            it.userId in sentRequests -> 1
            else -> 2
        }
    })

    LaunchedEffect(searchResults, currentSearchQuery) {
        if (searchQuery == currentSearchQuery) {
            isSearchEmpty = searchQuery.isNotBlank() && searchResults.isEmpty()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            navController = navController,
            viewModel = chatViewModel
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFC97C5D))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                BackButton(onBackClick = {
                    navController.navigate("main_screen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                })
            }

            item {
                Logo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SearchBar(
                    text = searchQuery,
                    onTextChanged = { searchQuery = it },
                    onSearch = {
                        currentSearchQuery = searchQuery
                        viewModel.searchUsers(searchQuery)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isSearchEmpty && searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "Người dùng không tồn tại!",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (sortedSearchResults.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSearchResultsExpanded = !isSearchResultsExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kết quả tìm kiếm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = if (isSearchResultsExpanded) 0f else 180f }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isSearchResultsExpanded) {
                    item {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = (5 * 48).dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortedSearchResults) { user ->
                                val isFriend = friends.any { it.userId == user.userId }
                                SearchResultItem(
                                    user = user,
                                    alreadySent = user.userId in sentRequests,
                                    onAddFriend = {
                                        viewModel.sendFriendRequest(user.userId)
                                        Toast.makeText(context, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                                    },
                                    onCancelFriendRequest = {
                                        viewModel.cancelFriendRequest(user.userId)
                                        Toast.makeText(context, "Đã hủy lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                                    },
                                    isFriend = isFriend,
                                    onProfileClick = {
                                        navController.navigate("competitor_profile/${user.userId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (receivedRequests.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFriendRequestsExpanded = !isFriendRequestsExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lời mời kết bạn",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = if (isFriendRequestsExpanded) 0f else 180f }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isFriendRequestsExpanded) {
                    item {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = (5 * 48).dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(receivedRequests) { request ->
                                FriendRequestItem(
                                    request = request,
                                    onAccept = {
                                        viewModel.acceptFriendRequest(request)
                                        Toast.makeText(context, "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show()
                                    },
                                    onDecline = {
                                        viewModel.declineFriendRequest(request)
                                        Toast.makeText(context, "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show()
                                    },
                                    onProfileClick = {
                                        navController.navigate("competitor_profile/${request.fromUserId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Hiển thị một mục trong kết quả tìm kiếm người dùng.
 *
 * @param user Thông tin người dùng.
 * @param alreadySent True nếu đã gửi lời mời kết bạn.
 * @param onAddFriend Hàm xử lý khi nhấn nút kết bạn.
 * @param onCancelFriendRequest Hàm xử lý khi hủy lời mời.
 * @param isFriend True nếu đã là bạn bè.
 * @param onProfileClick Hàm xử lý khi nhấn vào hồ sơ.
 */
@Composable
fun SearchResultItem(
    user: User,
    alreadySent: Boolean,
    onAddFriend: () -> Unit,
    onCancelFriendRequest: () -> Unit,
    isFriend: Boolean,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c), shape = RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = user.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        )
        if (!alreadySent && !isFriend) {
            Button(
                onClick = onAddFriend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.color_eed7c5),
                    contentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Gửi lời mời")
            }
        } else if (isFriend) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF388E3C),
                    disabledContentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Bạn bè")
            }
        } else {
            Button(
                onClick = onCancelFriendRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.color_b36a5e),
                    contentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Xóa lời mời")
            }
        }
    }
}

/**
 * Hiển thị một lời mời kết bạn.
 *
 * @param request Thông tin lời mời.
 * @param onAccept Hàm xử lý khi chấp nhận lời mời.
 * @param onDecline Hàm xử lý khi từ chối lời mời.
 * @param onProfileClick Hàm xử lý khi nhấn vào hồ sơ.
 */
@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = request.fromName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        )
        IconButton(
            onClick = onAccept,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.check),
                contentDescription = "Chấp nhận",
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(
            onClick = onDecline,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Từ chối",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}