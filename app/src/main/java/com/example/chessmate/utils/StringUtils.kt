package com.example.chessmate.utils

object StringUtils {
    fun generateSubstrings(text: String, minLength: Int = 1): List<String> {
        val substrings = mutableListOf<String>()
        val normalizedText = java.text.Normalizer.normalize(text.lowercase(), java.text.Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
        val words = normalizedText.split(" ").filter { it.isNotBlank() }
        if (normalizedText.isNotBlank()) {
            substrings.add(normalizedText)
        }
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
        return substrings.distinct()
    }
}