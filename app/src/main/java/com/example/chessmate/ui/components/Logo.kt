package com.example.chessmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.chessmate.R

/**
 * Hàm Composable hiển thị logo của ứng dụng Chessmate.
 *
 * @param modifier Bộ điều chỉnh giao diện cho logo.
 */
@Composable
fun Logo(modifier: Modifier = Modifier) {
    // Hiển thị logo và tiêu đề của ứng dụng
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hiển thị hình ảnh logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Chessmate Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}