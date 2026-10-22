package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Categories
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
  onDismiss: () -> Unit,
  onSave: (type: String, amount: Double, category: String, note: String) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Pengeluaran, 1: Pemasukan
  val isExpense = selectedTab == 0

  var amountText by remember { mutableStateOf("") }
  var noteText by remember { mutableStateOf("") }

  val categories = if (isExpense) Categories.allExpenseCategories else Categories.allIncomeCategories
  var selectedCategory by remember(isExpense) { mutableStateOf(categories.first()) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("add_transaction_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Catat Transaksi",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold
            )
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_add_dialog_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Tutup")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Type Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                "Pengeluaran",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) CoralExpense else MaterialTheme.colorScheme.onSurface
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                "Pemasukan",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) MintAccent else MaterialTheme.colorScheme.onSurface
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nominal Input
        OutlinedTextField(
          value = amountText,
          onValueChange = { input ->
            if (input.all { it.isDigit() }) {
              amountText = input
            }
          },
          label = { Text("Nominal (Rp)") },
          placeholder = { Text("Contoh: 25000") },
          leadingIcon = {
            Text(
              "Rp",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isExpense) CoralExpense else MintAccent
              ),
              modifier = Modifier.padding(start = 12.dp, end = 4.dp)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_amount_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Amount Chips
        Text(
          text = "Tambah Cepat:",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val quickAmounts = listOf(5_000, 10_000, 20_000, 50_000, 100_000)
          for (quick in quickAmounts) {
            val label = when {
              quick >= 1_000_000 -> "+${quick / 1_000_000}jt"
              else -> "+${quick / 1_000}k"
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                  val current = amountText.toDoubleOrNull() ?: 0.0
                  amountText = (current + quick).toLong().toString()
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selection
        Text(
          text = "Kategori:",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          for (cat in categories) {
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { selectedCategory = cat }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          label = { Text("Keterangan") },
          placeholder = { Text("Misal: Nasi Padang + Es Teh") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_note_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
          ) {
            Text("Batal")
          }

          Button(
            onClick = {
              val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
              if (parsedAmount > 0) {
                onSave(
                  if (isExpense) "EXPENSE" else "INCOME",
                  parsedAmount,
                  selectedCategory,
                  noteText
                )
              }
            },
            enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isExpense) CoralExpense else EmeraldPrimary
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("save_transaction_button")
          ) {
            Text("Simpan")
          }
        }
      }
    }
  }
}
