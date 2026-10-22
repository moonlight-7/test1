package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DailyBudgetMeter
import com.example.ui.components.TransactionCard
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val allTransactions by viewModel.allTransactions.collectAsState()
  val todayTransactions by viewModel.todayTransactions.collectAsState()
  val todaySpent by viewModel.todaySpent.collectAsState()
  val dailyLimit by viewModel.dailyBudgetLimit.collectAsState()
  val budgetHealth by viewModel.budgetHealth.collectAsState()
  val totalBalance by viewModel.totalBalance.collectAsState()
  val todayIncome by viewModel.todayIncome.collectAsState()

  val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
  }

  val todayDateString = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    // Header Banner with Student Brand
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                EmeraldDark,
                EmeraldPrimary
              )
            )
          )
          .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "RantauSmart",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "Anak Rantau 🎓",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }
              Text(
                text = todayDateString,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Total Balance Card
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "Total Sisa Saldo Kas",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White.copy(alpha = 0.85f),
                  fontWeight = FontWeight.Medium
                )
              )
              Text(
                text = currencyFormat.format(totalBalance).replace("Rp", "Rp "),
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(MintAccent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.ArrowDownward,
                      contentDescription = null,
                      tint = MintAccent,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = "Masuk Hari Ini",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    )
                    Text(
                      text = currencyFormat.format(todayIncome).replace("Rp", "Rp "),
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(CoralExpense.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.ArrowUpward,
                      contentDescription = null,
                      tint = Color(0xFFFF8A80),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "Keluar Hari Ini",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    )
                    Text(
                      text = currencyFormat.format(todaySpent).replace("Rp", "Rp "),
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Daily Budget Meter Card (Overlaps Banner slightly)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(top = 12.dp)
      ) {
        DailyBudgetMeter(
          todaySpent = todaySpent,
          dailyLimit = dailyLimit,
          health = budgetHealth,
          onEditBudgetClick = { viewModel.showBudgetDialog.value = true }
        )
      }
    }

    // Quick Action Bar
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Text(
          text = "Aksi Cepat",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // AI Chat Button
          Button(
            onClick = { viewModel.navigateTo(AppScreen.AI_CHAT) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("quick_ai_chat_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Chat AI Cepat", fontWeight = FontWeight.Bold)
            }
          }

          // Manual Add Button
          Button(
            onClick = { viewModel.showAddDialog.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("quick_add_manual_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Catat Manual", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Today's Transactions Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transaksi Hari Ini (${todayTransactions.size})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    if (todayTransactions.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Belum ada pengeluaran hari ini!",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
            )
            Text(
              text = "Bagus! Jaga uang sakumu tetap aman atau gunakan Chat AI untuk mencatat.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
              ),
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      items(todayTransactions, key = { it.id }) { item ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          TransactionCard(
            transaction = item,
            onDeleteClick = { viewModel.deleteTransaction(item) }
          )
        }
      }
    }

    // Recent Transactions Header
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Semua Riwayat Transaksi",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    val pastTransactions = allTransactions.filter { it !in todayTransactions }
    if (pastTransactions.isEmpty() && todayTransactions.isEmpty()) {
      item {
        Text(
          text = "Belum ada riwayat transaksi.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
          ),
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }
    } else {
      items(pastTransactions, key = { it.id }) { item ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          TransactionCard(
            transaction = item,
            onDeleteClick = { viewModel.deleteTransaction(item) }
          )
        }
      }
    }
  }
}
