package com.example.chessmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Hàm Composable tạo một nút tùy chỉnh với văn bản và màu nền.
 *
 * @param text Văn bản hiển thị trên nút.
 * @param colorId ID tài nguyên màu cho nền nút.
 * @param onClick Hàm gọi lại khi nút được nhấn (mặc định là hàm rỗng).
 */
@Composable
fun ButtonItem(
    text: String,
    colorId: Int,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(45.dp),
        shape = RoundedCornerShape(12.dp), // Bo góc nút
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = colorId))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}