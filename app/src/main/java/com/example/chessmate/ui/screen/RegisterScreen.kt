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
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                errorMessage = errorMessage,
                onRegisterClick = {
                    keyboardController?.hide()
                    if (password == confirmPassword) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    } else {
                        errorMessage = "Mật khẩu không khớp!"
                    }
                }
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
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InputField(label = "Email", value = email, onValueChange = onEmailChange)
        InputField(label = "Mật khẩu", value = password, onValueChange = onPasswordChange, isPassword = true)
        InputField(label = "Xác nhận mật khẩu", value = confirmPassword, onValueChange = onConfirmPasswordChange, isPassword = true)

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Đăng ký", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = label, fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(50.dp)
                .background(colorResource(id = R.color.color_eee2df), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = "Nhập $label ...", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                innerTextField()
            }
        )
    }
}
