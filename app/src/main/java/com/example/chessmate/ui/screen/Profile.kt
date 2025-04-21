package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Giao diện header cho màn hình hồ sơ cá nhân, hiển thị tiêu đề và nút quay lại.
 *
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun ProfileHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Row chứa nút quay lại, tiêu đề và khoảng trống để căn chỉnh
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        // Nút quay lại sử dụng biểu tượng từ tài nguyên drawable
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
        // Tiêu đề "Thông tin cá nhân" được căn giữa
        Text(
            text = "Thông tin cá nhân",
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
 * Thành phần hiển thị một hàng thông tin có thể chỉnh sửa trong hồ sơ cá nhân.
 *
 * @param label Nhãn của trường thông tin (ví dụ: "Tên:").
 * @param value Giá trị hiện tại của trường.
 * @param onValueChange Hàm xử lý khi giá trị thay đổi.
 */
@Composable
fun EditableProfileInfoRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    // Row chứa nhãn và trường nhập văn bản có thể chỉnh sửa
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nhãn của trường thông tin, cố định độ rộng 100dp
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        // Trường nhập văn bản cho phép người dùng chỉnh sửa
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Nội dung chính của màn hình hồ sơ cá nhân, hiển thị thông tin người dùng và các nút hành động.
 *
 * @param modifier Modifier tùy chỉnh giao diện.
 * @param userData Dữ liệu người dùng từ Firestore.
 * @param isEditing Trạng thái đang chỉnh sửa thông tin.
 * @param description Mô tả của người dùng.
 * @param onDescriptionChange Hàm xử lý khi mô tả thay đổi.
 * @param onEditClick Hàm xử lý khi nhấn nút chỉnh sửa.
 * @param onSaveClick Hàm xử lý khi nhấn nút lưu thông tin.
 * @param onMatchHistoryClick Hàm xử lý khi nhấn nút xem lịch sử trận đấu.
 * @param onLogoutClick Hàm xử lý khi nhấn nút đăng xuất.
 */
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userData: Map<String, Any>? = null,
    isEditing: Boolean = false,
    description: String = "",
    onDescriptionChange: (String) -> Unit = {},
    onEditClick: () -> Unit = {},
    onSaveClick: (String?) -> Unit = { _ -> },
    onMatchHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // Khởi tạo trạng thái cho tên có thể chỉnh sửa
    var editableName by remember { mutableStateOf(userData?.get("name")?.toString() ?: "") }
    // Tạo trạng thái cuộn cho nội dung
    val scrollState = rememberScrollState()

    // Column chính chứa toàn bộ nội dung hồ sơ
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c97c5d))
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        // Ảnh đại diện người dùng, hiển thị dạng hình tròn
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Ảnh đại diện",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Hàng chứa các nút "Sửa thông tin" và "Đổi ảnh"
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nút chuyển đổi giữa chế độ chỉnh sửa và lưu
            Button(
                onClick = {
                    if (isEditing) {
                        onSaveClick(editableName)
                    } else {
                        onEditClick()
                    }
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isEditing) "Lưu" else "Sửa thông tin",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            // Nút đổi ảnh, hiện chưa được triển khai
            Button(
                onClick = { /* TODO: Xử lý đổi ảnh */ },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Đổi ảnh",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Hàng chứa các nút "Lịch sử trận đấu" và "Đăng xuất"
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nút xem lịch sử trận đấu
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
            // Nút đăng xuất
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Khung chứa thông tin cá nhân
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .border(1.dp, Color.Black)
                .background(colorResource(id = R.color.color_eee2df))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hiển thị thông tin có thể chỉnh sửa nếu đang ở chế độ chỉnh sửa
            if (isEditing) {
                EditableProfileInfoRow(label = "Tên:", value = editableName, onValueChange = { editableName = it })
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Email:", value = FirebaseAuth.getInstance().currentUser?.email ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Ngày tạo:", value = userData?.get("createdAt")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Điểm:", value = userData?.get("score")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                // Trường nhập mô tả có thể chỉnh sửa
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mô tả:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.width(100.dp)
                            )
                            innerTextField()
                        }
                    }
                )
            } else {
                // Hiển thị thông tin chỉ đọc nếu không ở chế độ chỉnh sửa
                ProfileInfoRow(label = "Tên:", value = userData?.get("name")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Email:", value = FirebaseAuth.getInstance().currentUser?.email ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Ngày tạo:", value = userData?.get("createdAt")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Điểm:", value = userData?.get("score")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Mô tả:", value = description)
            }
        }
    }
}

/**
 * Thành phần hiển thị một hàng thông tin không thể chỉnh sửa trong hồ sơ cá nhân.
 *
 * @param label Nhãn của trường thông tin (ví dụ: "Email:").
 * @param value Giá trị của trường.
 */
@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    // Row chứa nhãn và giá trị thông tin chỉ đọc
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nhãn của trường thông tin
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        // Giá trị của trường thông tin
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Màn hình chính hiển thị hồ sơ cá nhân của người dùng, hỗ trợ chỉnh sửa thông tin và đăng xuất.
 *
 * @param navController Điều hướng để chuyển đến các màn hình khác.
 * @param onBackClick Hàm xử lý khi nhấn nút quay lại.
 * @param onMatchHistoryClick Hàm xử lý khi nhấn nút xem lịch sử trận đấu.
 */
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() },
    onMatchHistoryClick: () -> Unit = {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            navController?.navigate("match_history/$userId")
        }
    },
) {
    // Khởi tạo các biến trạng thái và tham chiếu Firebase
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isFetchingData by remember { mutableStateOf(true) }

    // Biến lưu tên có thể chỉnh sửa
    var editableName by remember { mutableStateOf("") }

    // Cập nhật editableName khi userData thay đổi
    LaunchedEffect(userData) {
        editableName = userData?.get("name")?.toString() ?: ""
    }

    // Tải dữ liệu người dùng từ Firestore khi màn hình được tạo
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.data
                        description = document.getString("description") ?: ""
                        editableName = document.getString("name") ?: ""
                    }
                    isFetchingData = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi khi tải thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                    isFetchingData = false
                }
        } else {
            // Chuyển hướng đến màn hình đăng nhập nếu chưa đăng nhập
            navController?.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            isFetchingData = false
        }
    }

    // Giao diện chính của màn hình hồ sơ
    Column(modifier = Modifier.fillMaxSize()) {
        // Header chứa nút quay lại và tiêu đề
        ProfileHeader(onBackClick = onBackClick)
        if (isFetchingData) {
            // Hiển thị vòng tròn tải khi đang lấy dữ liệu
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Nội dung hồ sơ cá nhân
            ProfileContent(
                modifier = Modifier.fillMaxWidth().weight(1f),
                userData = userData,
                isEditing = isEditing,
                description = description,
                onDescriptionChange = { description = it },
                onEditClick = {
                    isEditing = true
                    editableName = userData?.get("name")?.toString() ?: ""
                },
                onSaveClick = { currentEditableName ->
                    if (isEditing) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            // Cập nhật tên nếu tên mới khác tên cũ
                            if (userData?.get("name")?.toString() != currentEditableName && currentEditableName != null) {
                                firestore.collection("users")
                                    .document(userId)
                                    .update("name", currentEditableName)
                                    .addOnSuccessListener {
                                        userData = userData?.toMutableMap()?.apply { put("name", currentEditableName) }
                                        Toast.makeText(context, "Đã cập nhật tên.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Lỗi khi cập nhật tên: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // Cập nhật mô tả
                            firestore.collection("users")
                                .document(userId)
                                .update("description", description)
                                .addOnSuccessListener {
                                    isEditing = false
                                    Toast.makeText(context, "Đã lưu thông tin.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi khi lưu mô tả: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                onMatchHistoryClick = onMatchHistoryClick,
                onLogoutClick = {
                    // Đăng xuất và chuyển hướng về màn hình chính
                    auth.signOut()
                    Toast.makeText(context, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    navController?.navigate("home") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}