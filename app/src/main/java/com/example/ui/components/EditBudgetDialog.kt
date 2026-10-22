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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBudgetDialog(
  currentLimit: Double,
  onDismiss: () -> Unit,
  onSave: (Double) -> Unit
) {
  var limitText by remember { mutableStateOf(currentLimit.toLong().toString()) }

  val presets = listOf(30_000, 40_000, 50_000, 75_000, 100_000)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("edit_budget_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Batas Anggaran Harian",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Tutup")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Tentukan batas pengeluaran maksimal sehari agar uang kiriman orang tua tidak cepat habis.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = limitText,
          onValueChange = { input ->
            if (input.all { it.isDigit() }) {
              limitText = input
            }
          },
          label = { Text("Nominal Batas Harian") },
          leadingIcon = {
            Text(
              "Rp",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(start = 12.dp, end = 4.dp)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_budget_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Pilihan Preset Mahasiswa:",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          for (preset in presets) {
            val isSelected = limitText == preset.toString()
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { limitText = preset.toString() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Rp ${preset / 1000}k",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
              val parsed = limitText.toDoubleOrNull() ?: 50000.0
              if (parsed > 0) {
                onSave(parsed)
              }
            },
            modifier = Modifier
              .weight(1f)
              .testTag("save_daily_budget_button")
          ) {
            Text("Simpan")
          }
        }
      }
    }
  }
}
