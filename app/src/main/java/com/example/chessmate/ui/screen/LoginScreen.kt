package com.example.chessmate.ui.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@Composable
fun Header(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFC89F9C))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Đăng nhập",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LoginForm(onLoginClick: (String, String) -> Unit, onGoogleLoginClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        @Composable
        fun InputField(value: String, onValueChange: (String) -> Unit, hint: String, isPassword: Boolean = false) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(hint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 16.sp),
                shape = RoundedCornerShape(20.dp),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text)
            )
        }

        InputField(value = email, onValueChange = { email = it }, hint = "Email")
        InputField(value = password, onValueChange = { password = it }, hint = "Mật khẩu", isPassword = true)

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Vui lòng điền đầy đủ thông tin."
                } else {
                    errorMessage = ""
                    keyboardController?.hide()
                    onLoginClick(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c))
        ) { Text("Đăng nhập", fontSize = 20.sp) }

        Button(
            onClick = onGoogleLoginClick,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c))
        ) {
            Image(painter = painterResource(id = R.drawable.google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng nhập với Google", fontSize = 16.sp)
        }
    }
}


@Composable
fun LoginScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val oneTapClient: SignInClient = Identity.getSignInClient(context)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            credential.googleIdToken?.let {
                val firebaseCredential = GoogleAuthProvider.getCredential(it, null)
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) navController?.navigate("main_screen")
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController?.navigate("main_screen")
            } else {
                Toast.makeText(context, "Đăng nhập thất bại. Kiểm tra lại tài khoản hoặc mật khẩu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(onBackClick = { navController?.popBackStack() })
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFC97C5D)).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Logo(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))
            LoginForm(
                onLoginClick = { email, password -> loginWithEmail(email, password) },
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
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    ChessmateTheme { LoginScreen() }
}