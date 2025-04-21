package com.example.chessmate.utils

/**
 * Đối tượng tiện ích để xử lý chuỗi, cung cấp các phương thức liên quan đến việc tạo và xử lý chuỗi con.
 */
object StringUtils {

    /**
     * Tạo danh sách các chuỗi con từ một chuỗi văn bản đầu vào, bao gồm cả chuỗi đã chuẩn hóa và các chuỗi con của từng từ.
     *
     * @param text Chuỗi văn bản đầu vào.
     * @param minLength Độ dài tối thiểu của các chuỗi con (mặc định là 1).
     * @return Danh sách các chuỗi con duy nhất, đã được chuẩn hóa và phân tách.
     */
    fun generateSubstrings(text: String, minLength: Int = 1): List<String> {
        val substrings = mutableListOf<String>()
        // Chuẩn hóa chuỗi: chuyển về chữ thường, loại bỏ dấu và các ký tự không mong muốn
        val normalizedText = java.text.Normalizer.normalize(text.lowercase(), java.text.Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
        // Tách chuỗi thành các từ, lọc bỏ các từ trống
        val words = normalizedText.split(" ").filter { it.isNotBlank() }
        // Thêm chuỗi đã chuẩn hóa vào danh sách nếu không rỗng
        if (normalizedText.isNotBlank()) {
            substrings.add(normalizedText)
        }
        // Tạo các chuỗi con từ từng từ
        for (word in words) {
            for (i in 0 until word.length) {
                for (j in i + minLength..word.length) {
                    val substring = word.substring(i, j)
                    if (substring.length >= minLength) {
                        substrings.add(substring)
                    }
                }
            }
        }
        // Trả về danh sách các chuỗi con duy nhất
        return substrings.distinct()
    }
}