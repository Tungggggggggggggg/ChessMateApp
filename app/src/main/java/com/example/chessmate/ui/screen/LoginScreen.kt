package com.example.chessmate.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.chessmate.utils.StringUtils

/**
 * Giao diện header cho màn hình đăng nhập, hiển thị tiêu đề và nút quay lại.
 *
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 */
@Composable
fun Header(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFC89F9C))
            .padding(vertical = 10.dp)
    ) {
        // Nút quay lại
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
        // Tiêu đề đăng nhập
        Text(
            text = "Đăng nhập",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

/**
 * Form nhập thông tin đăng nhập, bao gồm tài khoản, mật khẩu và các tùy chọn đăng nhập Google hoặc chuyển sang đăng ký.
 *
 * @param onLoginClick Hàm xử lý đăng nhập với tài khoản và mật khẩu.
 * @param onGoogleLoginClick Hàm xử lý đăng nhập bằng Google.
 * @param onNavigateToRegister Hàm xử lý chuyển hướng đến màn hình đăng ký.
 */
@Composable
fun LoginForm(onLoginClick: (String, String) -> Unit, onGoogleLoginClick: () -> Unit, onNavigateToRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /**
         * Thành phần nhập liệu cho tài khoản hoặc mật khẩu.
         *
         * @param label Nhãn của trường nhập liệu.
         * @param value Giá trị hiện tại của trường.
         * @param onValueChange Hàm xử lý khi giá trị thay đổi.
         * @param isPassword Xác định trường có phải là mật khẩu hay không.
         * @param placeholder Văn bản gợi ý khi trường trống.
         * @param errorMessage Thông báo lỗi nếu có.
         */
        @Composable
        fun InputField(
            label: String,
            value: String,
            onValueChange: (String) -> Unit,
            isPassword: Boolean = false,
            placeholder: String,
            errorMessage: String? = null
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.color_eee2df), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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

        // Trường nhập tài khoản
        InputField(
            label = "Tài khoản:",
            value = username,
            onValueChange = { username = it },
            placeholder = "Nhập tài khoản ...",
            errorMessage = usernameError
        )
        // Trường nhập mật khẩu
        InputField(
            label = "Mật khẩu:",
            value = password,
            onValueChange = { password = it },
            isPassword = true,
            placeholder = "Nhập mật khẩu ...",
            errorMessage = passwordError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút đăng nhập
        Button(
            onClick = {
                usernameError = null
                passwordError = null

                var hasError = false
                if (username.isBlank()) {
                    usernameError = "Bạn chưa điền Tài khoản"
                    hasError = true
                }
                if (password.isBlank()) {
                    passwordError = "Bạn chưa điền Mật khẩu"
                    hasError = true
                }

                if (!hasError) {
                    keyboardController?.hide()
                    onLoginClick(username, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c))
        ) {
            Text("Đăng nhập", fontSize = 16.sp)
        }

        // Nút đăng nhập bằng Google
        Button(
            onClick = onGoogleLoginClick,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c))
        ) {
            Image(painter = painterResource(id = R.drawable.google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng nhập với Google", fontSize = 16.sp)
        }
        // Nút chuyển sang màn hình đăng ký
        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Chưa có tài khoản?",
                fontSize = 14.sp,
                color = Color.White,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

/**
 * Màn hình chính để đăng nhập vào ứng dụng, hỗ trợ đăng nhập bằng tài khoản/mật khẩu hoặc Google.
 *
 * @param navController Điều hướng để chuyển đến các màn hình khác.
 */
@Composable
fun LoginScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val oneTapClient: SignInClient = Identity.getSignInClient(context)
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Xử lý kết quả đăng nhập bằng Google
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            credential.googleIdToken?.let { idToken ->
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val userId = user.uid
                            firestore.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                        navController?.navigate("main_screen") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        val name = user.displayName ?: "Unknown"
                                        val email = user.email ?: ""
                                        val nameLowercase = name.lowercase()
                                        val nameKeywords = StringUtils.generateSubstrings(name)
                                        val userData = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "createdAt" to currentDate,
                                            "description" to "Không có mô tả",
                                            "score" to 0,
                                            "userId" to userId,
                                            "nameLowercase" to nameLowercase,
                                            "nameKeywords" to nameKeywords
                                        )
                                        firestore.collection("users")
                                            .document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                                navController?.navigate("main_screen") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Lỗi khi lưu dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                                                auth.signOut()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi khi truy cập dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                }
                        } else {
                            Toast.makeText(context, "Lỗi: Không thể lấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    } else {
                        Toast.makeText(context, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Đăng nhập thất bại: Không nhận được kết quả từ Google.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Xử lý đăng nhập bằng tài khoản và mật khẩu.
     *
     * @param username Tên tài khoản người dùng nhập.
     * @param password Mật khẩu người dùng nhập.
     */
    fun loginWithUsername(username: String, password: String) {
        val email = "$username@chessmate.com"
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                navController?.navigate("main_screen") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                val name = username
                                val nameLowercase = name.lowercase()
                                val nameKeywords = StringUtils.generateSubstrings(name)
                                val userData = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "createdAt" to currentDate,
                                    "description" to "Không có mô tả",
                                    "score" to 0,
                                    "userId" to userId,
                                    "username" to username,
                                    "nameLowercase" to nameLowercase,
                                    "nameKeywords" to nameKeywords
                                )
                                firestore.collection("users")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                        navController?.navigate("main_screen") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Lỗi khi lưu dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Lỗi khi truy cập dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                } else {
                    Toast.makeText(context, "Lỗi: Không thể lấy ID người dùng sau đăng nhập.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            } else {
                Toast.makeText(
                    context,
                    "Đăng nhập thất bại. Kiểm tra lại tài khoản hoặc mật khẩu.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header hiển thị tiêu đề và nút quay lại
        Header(onBackClick = {
            if (navController?.previousBackStackEntry == null) {
                navController?.navigate("home") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.popBackStack()
            }
        })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFC97C5D))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo ứng dụng
            Logo(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))
            // Form đăng nhập
            LoginForm(
                onLoginClick = { username, password -> loginWithUsername(username, password) },
                onGoogleLoginClick = {
                    oneTapClient.beginSignIn(
                        BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId("734321254855-higdjv79hl6msi0ilm4n8ifih4j3j1rp.apps.googleusercontent.com")
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            )
                            .build()
                    ).addOnSuccessListener { result ->
                        launcher.launch(androidx.activity.result.IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Lỗi khi bắt đầu đăng nhập Google: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onNavigateToRegister = {
                    navController?.navigate("register")
                }
            )
        }
    }
}