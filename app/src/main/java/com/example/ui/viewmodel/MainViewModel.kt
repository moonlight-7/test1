package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetPreferences
import com.example.data.model.Categories
import com.example.data.model.ChatMessage
import com.example.data.model.SenderType
import com.example.data.model.TransactionEntity
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
  HOME,
  AI_CHAT,
  SUMMARY
}

enum class SummaryPeriod {
  TODAY,
  THIS_WEEK,
  THIS_MONTH
}

enum class BudgetHealth {
  SAFE,      // < 80%
  WARNING,   // 80% - 100%
  EXCEEDED   // > 100%
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val preferences = BudgetPreferences(application)
  private val repository = TransactionRepository(database, preferences)

  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val dailyBudgetLimit: StateFlow<Double> = repository.dailyBudgetLimit

  // Today's Expense and Income calculations
  val todayTransactions: StateFlow<List<TransactionEntity>> = allTransactions.combine(_currentScreen) { list, _ ->
    val startOfDay = TransactionRepository.getStartOfDay()
    val endOfDay = TransactionRepository.getEndOfDay()
    list.filter { it.timestamp in startOfDay..endOfDay }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todaySpent: StateFlow<Double> = todayTransactions.combine(_currentScreen) { list, _ ->
    list.filter { it.isExpense }.sumOf { it.amount }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val todayIncome: StateFlow<Double> = todayTransactions.combine(_currentScreen) { list, _ ->
    list.filter { !it.isExpense }.sumOf { it.amount }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val totalBalance: StateFlow<Double> = allTransactions.combine(_currentScreen) { list, _ ->
    val income = list.filter { !it.isExpense }.sumOf { it.amount }
    val expense = list.filter { it.isExpense }.sumOf { it.amount }
    income - expense
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val budgetHealth: StateFlow<BudgetHealth> = combine(todaySpent, dailyBudgetLimit) { spent, limit ->
    when {
      limit <= 0.0 -> BudgetHealth.SAFE
      spent > limit -> BudgetHealth.EXCEEDED
      spent >= limit * 0.8 -> BudgetHealth.WARNING
      else -> BudgetHealth.SAFE
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetHealth.SAFE)

  // AI Chat Conversation State
  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        sender = SenderType.AI,
        text = "Halo sobat rantau! 👋 Aku asisten keuanganmu di RantauSmart. Tuliskan pengeluaran atau pemasukanmu secara bebas (misal: 'Makan siang warteg 18k' atau 'Dikirimi uang bulanan 1.5jt'). Aku akan langsung menyimpannya otomatis!"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _isAiThinking = MutableStateFlow(false)
  val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

  // Summary Screen Filter
  private val _summaryPeriod = MutableStateFlow(SummaryPeriod.THIS_MONTH)
  val summaryPeriod: StateFlow<SummaryPeriod> = _summaryPeriod.asStateFlow()

  // Dialog Visibility States
  var showAddDialog = MutableStateFlow(false)
  var showBudgetDialog = MutableStateFlow(false)
  var showResetDialog = MutableStateFlow(false)

  init {
    seedInitialDataIfEmpty()
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun setSummaryPeriod(period: SummaryPeriod) {
    _summaryPeriod.value = period
  }

  fun updateDailyBudget(newLimit: Double) {
    repository.setDailyBudgetLimit(newLimit)
    showBudgetDialog.value = false
  }

  fun addManualTransaction(type: String, amount: Double, category: String, note: String) {
    viewModelScope.launch {
      val entity = TransactionEntity(
        type = type,
        amount = amount,
        category = category,
        note = note.ifBlank { category },
        timestamp = System.currentTimeMillis(),
        source = "MANUAL"
      )
      repository.insertTransaction(entity)
      showAddDialog.value = false
    }
  }

  fun deleteTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      repository.deleteTransaction(transaction)
    }
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return

    val userMessage = ChatMessage(
      sender = SenderType.USER,
      text = text.trim()
    )

    _chatMessages.value = _chatMessages.value + userMessage
    _isAiThinking.value = true

    viewModelScope.launch {
      try {
        val (aiMessage, _) = repository.processChatMessage(text)
        _chatMessages.value = _chatMessages.value + aiMessage
      } catch (e: Exception) {
        val errorMessage = ChatMessage(
          sender = SenderType.AI,
          text = "Maaf, terjadi kendala saat memproses: ${e.localizedMessage ?: "Coba ulangi lagi ya"}",
          isError = true
        )
        _chatMessages.value = _chatMessages.value + errorMessage
      } finally {
        _isAiThinking.value = false
      }
    }
  }

  fun undoChatMessageTransaction(chatMessage: ChatMessage) {
    val transaction = chatMessage.transaction ?: return
    viewModelScope.launch {
      repository.deleteTransactionById(transaction.id)
      // Update chat message indicating undone
      val updatedList = _chatMessages.value.map {
        if (it.id == chatMessage.id) {
          it.copy(
            text = "❌ Transaksi sebesar ${transaction.formattedAmount} telah dibatalkan & dihapus dari catatan.",
            transaction = null
          )
        } else it
      }
      _chatMessages.value = updatedList
    }
  }

  fun resetAllData() {
    viewModelScope.launch {
      repository.deleteAllTransactions()
      showResetDialog.value = false
    }
  }

  private fun seedInitialDataIfEmpty() {
    viewModelScope.launch {
      val existing = database.transactionDao().getTransactionsInRangeSync(0, System.currentTimeMillis() + 86400000)
      if (existing.isEmpty()) {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600000L
        val threeHoursAgo = now - 10800000L
        val yesterday = now - 86400000L
        val twoDaysAgo = now - 172800000L

        val initialList = listOf(
          TransactionEntity(
            type = "INCOME",
            amount = 1500000.0,
            category = "Uang Saku / Kiriman Ortu",
            note = "Kiriman bulanan orang tua",
            timestamp = twoDaysAgo,
            source = "MANUAL"
          ),
          TransactionEntity(
            type = "EXPENSE",
            amount = 500000.0,
            category = Categories.TEMPAT_TINGGAL,
            note = "Iuran sewa kos bulanan",
            timestamp = twoDaysAgo + 3600000L,
            source = "MANUAL"
          ),
          TransactionEntity(
            type = "EXPENSE",
            amount = 22000.0,
            category = Categories.TRANSPORTASI,
            note = "Isi bensin motor Pertalite",
            timestamp = yesterday,
            source = "AI_CHAT"
          ),
          TransactionEntity(
            type = "EXPENSE",
            amount = 18000.0,
            category = Categories.MAKANAN,
            note = "Makan siang nasi rames + es teh",
            timestamp = threeHoursAgo,
            source = "AI_CHAT"
          ),
          TransactionEntity(
            type = "EXPENSE",
            amount = 12000.0,
            category = Categories.KEBUTUHAN_HARIAN,
            note = "Air galon isi ulang + sabun",
            timestamp = oneHourAgo,
            source = "MANUAL"
          )
        )

        for (item in initialList) {
          repository.insertTransaction(item)
        }
      }
    }
  }
}
