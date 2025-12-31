package com.anekabaru.anbkasir.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

fun Double.toRupiah(): String {
    val formatter = NumberFormat.getInstance(Locale("in", "ID"))
    return "Rp${formatter.format(this)}"
}

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted =
            NumberFormat.getInstance(Locale("in", "ID")).format(originalText.toLongOrNull() ?: 0L)
        val annotatedString = AnnotatedString(formatted)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val sub = originalText.substring(0, offset.coerceAtMost(originalText.length))
                return NumberFormat.getInstance(Locale("in", "ID"))
                    .format(sub.toLongOrNull() ?: 0L).length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val digitsOnly =
                    annotatedString.text.substring(0, offset.coerceAtMost(annotatedString.length))
                        .replace(".", "")
                return digitsOnly.length.coerceAtMost(originalText.length)
            }
        }
        return TransformedText(annotatedString, offsetMapping)
    }
}