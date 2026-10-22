package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BudgetPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.SenderType
import com.example.data.model.TransactionEntity
import com.example.data.network.GeminiApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class TransactionRepository(
  private val database: AppDatabase,
  private val budgetPreferences: BudgetPreferences,
  private val geminiApiClient: GeminiApiClient = GeminiApiClient()
) {
  private val dao = database.transactionDao()

  val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
  val dailyBudgetLimit: StateFlow<Double> = budgetPreferences.dailyBudgetLimit

  fun setDailyBudgetLimit(limit: Double) {
    budgetPreferences.setDailyBudgetLimit(limit)
  }

  suspend fun insertTransaction(transaction: TransactionEntity): Long {
    return dao.insertTransaction(transaction)
  }

  suspend fun deleteTransaction(transaction: TransactionEntity) {
    dao.deleteTransaction(transaction)
  }

  suspend fun deleteTransactionById(id: Long) {
    dao.deleteById(id)
  }

  suspend fun deleteAllTransactions() {
    dao.deleteAll()
  }

  /**
   * Processes a user chat message:
   * 1. Extracts financial transaction details using Gemini / local NLP
   * 2. Automatically saves the transaction to Room Database
   * 3. Returns the AI reply with attached transaction for visual feedback & undo actions
   */
  suspend fun processChatMessage(userText: String): Pair<ChatMessage, TransactionEntity?> {
    val parseResult = geminiApiClient.parseUserMessage(userText)

    if (parseResult.isTransaction && parseResult.amount > 0) {
      val entity = TransactionEntity(
        type = parseResult.type,
        amount = parseResult.amount,
        category = parseResult.category,
        note = parseResult.note.ifBlank { "Transaksi via Chat AI" },
        timestamp = System.currentTimeMillis(),
        source = "AI_CHAT"
      )
      val insertedId = dao.insertTransaction(entity)
      val savedEntity = entity.copy(id = insertedId)

      val aiMessage = ChatMessage(
        sender = SenderType.AI,
        text = parseResult.replyMessage,
        transaction = savedEntity
      )
      return Pair(aiMessage, savedEntity)
    } else {
      val aiMessage = ChatMessage(
        sender = SenderType.AI,
        text = "Hai kawan rantau! Aku tidak mendeteksi nominal transaksi dari pesanmu. Coba ketik seperti: \"Makan siang warteg 18rb\", \"Beli detergen 25k\", atau \"Dapat kiriman uang saku 1.5jt\" ya! 😊"
      )
      return Pair(aiMessage, null)
    }
  }

  companion object {
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
      val cal = Calendar.getInstance()
      cal.timeInMillis = timestamp
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
      val cal = Calendar.getInstance()
      cal.timeInMillis = timestamp
      cal.set(Calendar.HOUR_OF_DAY, 23)
      cal.set(Calendar.MINUTE, 59)
      cal.set(Calendar.SECOND, 59)
      cal.set(Calendar.MILLISECOND, 999)
      return cal.timeInMillis
    }

    fun getStartOfWeek(): Long {
      val cal = Calendar.getInstance()
      cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      return cal.timeInMillis
    }

    fun getStartOfMonth(): Long {
      val cal = Calendar.getInstance()
      cal.set(Calendar.DAY_OF_MONTH, 1)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      return cal.timeInMillis
    }
  }
}
