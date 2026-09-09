package com.formautomation.domain.automation

import android.view.KeyEvent
import kotlinx.coroutines.delay

data class InputEvent(
    val keyCode: Int,
    val character: Char,
    val timestamp: Long = System.currentTimeMillis()
)

data class TypingState(
    val fieldId: String,
    val currentPosition: Int = 0,
    val totalLength: Int = 0,
    val typedValue: String = "",
    val isComplete: Boolean = false
)

class TypingSimulator {
    suspend fun typeLetterByLetter(
        text: String,
        onKeyPress: suspend (Char) -> Unit,
        delayBetweenChars: Long = 50, // milliseconds
        delayBeforeSend: Long = 100
    ): Result<TypingState> {
        return try {
            var position = 0
            val typedText = StringBuilder()

            delay(delayBeforeSend)

            for (char in text) {
                try {
                    onKeyPress(char)
                    typedText.append(char)
                    position++

                    // Realistic delay between keystrokes
                    delay(delayBetweenChars + (0..30).random())
                } catch (e: Exception) {
                    return Result.failure(
                        Exception("Error typing character '$char' at position $position: ${e.message}")
                    )
                }
            }

            Result.success(
                TypingState(
                    fieldId = "",
                    currentPosition = position,
                    totalLength = text.length,
                    typedValue = typedText.toString(),
                    isComplete = position == text.length
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun continueTypingFromPosition(
        text: String,
        startPosition: Int,
        onKeyPress: suspend (Char) -> Unit,
        delayBetweenChars: Long = 50
    ): Result<TypingState> {
        return try {
            if (startPosition >= text.length) {
                return Result.success(
                    TypingState(
                        fieldId = "",
                        currentPosition = startPosition,
                        totalLength = text.length,
                        typedValue = text,
                        isComplete = true
                    )
                )
            }

            var position = startPosition
            val typedText = StringBuilder(text.substring(0, startPosition))

            for (i in startPosition until text.length) {
                val char = text[i]
                try {
                    onKeyPress(char)
                    typedText.append(char)
                    position++

                    delay(delayBetweenChars + (0..30).random())
                } catch (e: Exception) {
                    return Result.failure(
                        Exception("Error continuing from position $startPosition at char '$char': ${e.message}")
                    )
                }
            }

            Result.success(
                TypingState(
                    fieldId = "",
                    currentPosition = position,
                    totalLength = text.length,
                    typedValue = typedText.toString(),
                    isComplete = position == text.length
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
