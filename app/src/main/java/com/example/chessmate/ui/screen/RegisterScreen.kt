package com.example.chessmate.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.chessmate.utils.StringUtils

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    suspend fun checkUsernameExists(username: String): Boolean {
        val query = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
        return !query.isEmpty
    }

    fun handleRegister() {
        nameError = null
        usernameError = null
        passwordError = null
        confirmPasswordError = null

        var hasError = false
        if (name.isBlank()) {
            nameError = "Bạn chưa điền Tên"
            hasError = true
        }
        if (username.isBlank()) {
            usernameError = "Bạn chưa điền Tài khoản"
            hasError = true
        }
        if (password.isBlank()) {
            passwordError = "Bạn chưa điền Mật khẩu"
            hasError = true
        } else if (password.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            hasError = true
        }
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Bạn chưa điền Xác nhận mật khẩu"
            hasError = true
        }

        if (!hasError) {
            keyboardController?.hide()
            if (password != confirmPassword) {
                confirmPasswordError = "Mật khẩu không khớp!"
                return
            }

            coroutineScope.launch {
                try {
                    val usernameExists = checkUsernameExists(username)
                    if (usernameExists) {
                        usernameError = "Tài khoản đã tồn tại"
                        return@launch
                    }

                    isRegistering = true
                    val email = "$username@chessmate.com"
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { authtask ->
                            if (authtask.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: run {
                                    Toast.makeText(context, "Không thể lấy ID người dùng.", Toast.LENGTH_SHORT).show()
                                    isRegistering = false
                                    return@addOnCompleteListener
                                }
                                val createdAt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                val nameLowercase = name.lowercase()
                                val nameKeywords = StringUtils.generateSubstrings(name)
                                val user = hashMapOf(
                                    "userId" to userId,
                                    "name" to name,
                                    "username" to username,
                                    "email" to email,
                                    "createdAt" to createdAt,
                                    "description" to "Không có mô tả",
                                    "score" to 0,
                                    "nameLowercase" to nameLowercase,
                                    "nameKeywords" to nameKeywords
                                )

                                firestore.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                        isRegistering = false
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Lỗi khi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                                        isRegistering = false
                                        auth.currentUser?.delete()
                                    }
                            } else {
                                Toast.makeText(context, authtask.exception?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                                isRegistering = false
                            }
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi khi kiểm tra tài khoản: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
    ) {
        RegisterHeader(onBackClick = { navController.popBackStack() })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colorResource(id = R.color.color_c97c5d))
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Logo(modifier = Modifier.fillMaxWidth().wrapContentHeight())
            Spacer(modifier = Modifier.height(20.dp))
            RegisterForm(
                name = name,
                onNameChange = { name = it },
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                nameError = nameError,
                usernameError = usernameError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                isRegistering = isRegistering,
                onRegisterClick = { handleRegister() }
            )
        }
    }
}

@Composable
fun RegisterHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 20.dp),
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
            text = "Đăng kí",
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

@Composable
fun RegisterForm(
    name: String,
    onNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    nameError: String?,
    usernameError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    isRegistering: Boolean,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InputField(
            label = "Tên",
            value = name,
            onValueChange = onNameChange,
            errorMessage = nameError
        )
        InputField(
            label = "Tài khoản",
            value = username,
            onValueChange = onUsernameChange,
            errorMessage = usernameError
        )
        InputField(
            label = "Mật khẩu",
            value = password,
            onValueChange = onPasswordChange,
            isPassword = true,
            errorMessage = passwordError
        )
        InputField(
            label = "Xác nhận mật khẩu",
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            isPassword = true,
            errorMessage = confirmPasswordError
        )

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
            shape = RoundedCornerShape(20.dp),
            enabled = !isRegistering
        ) {
            Text("Đăng ký", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = label, fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(id = R.color.color_eee2df), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = "Nhập $label ...", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                innerTextField()
            }
        )
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}