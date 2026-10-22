package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SenderType {
  USER,
  AI
}

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: SenderType,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val transaction: TransactionEntity? = null,
  val isError: Boolean = false
) {
  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
      return sdf.format(Date(timestamp))
    }
}
