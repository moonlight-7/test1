package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.TransactionEntity
import com.example.data.repository.TransactionRepository
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SummaryPeriod
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummaryScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val allTransactions by viewModel.allTransactions.collectAsState()
  val selectedPeriod by viewModel.summaryPeriod.collectAsState()
  val context = LocalContext.current

  val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
  }

  // Filter transactions based on selected period
  val filteredTransactions = remember(allTransactions, selectedPeriod) {
    val now = System.currentTimeMillis()
    val startTime = when (selectedPeriod) {
      SummaryPeriod.TODAY -> TransactionRepository.getStartOfDay(now)
      SummaryPeriod.THIS_WEEK -> TransactionRepository.getStartOfWeek()
      SummaryPeriod.THIS_MONTH -> TransactionRepository.getStartOfMonth()
    }
    allTransactions.filter { it.timestamp >= startTime }
  }

  val totalIncome = filteredTransactions.filter { !it.isExpense }.sumOf { it.amount }
  val totalExpense = filteredTransactions.filter { it.isExpense }.sumOf { it.amount }
  val netSavings = totalIncome - totalExpense

  // Category breakdown for expenses
  val expenseByCategory = remember(filteredTransactions) {
    filteredTransactions
      .filter { it.isExpense }
      .groupBy { it.category }
      .mapValues { (_, txs) -> txs.sumOf { it.amount } }
      .toList()
      .sortedByDescending { it.second }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("summary_screen"),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
  ) {
    // Screen Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Ringkasan Finansial",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.ExtraBold
            )
          )
          Text(
            text = "Evaluasi arus kas dan kebiasaan belanja anak kos",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          )
        }

        // Share report button
        OutlinedButton(
          onClick = {
            val periodLabel = when (selectedPeriod) {
              SummaryPeriod.TODAY -> "Hari Ini"
              SummaryPeriod.THIS_WEEK -> "Minggu Ini"
              SummaryPeriod.THIS_MONTH -> "Bulan Ini"
            }
            val reportText = buildString {
              appendLine("📊 *Laporan Keuangan RantauSmart ($periodLabel)*")
              appendLine("💰 Total Pemasukan: ${currencyFormat.format(totalIncome)}")
              appendLine("💸 Total Pengeluaran: ${currencyFormat.format(totalExpense)}")
              appendLine("📦 Sisa Kas: ${currencyFormat.format(netSavings)}")
              appendLine("\n*Rincian Pengeluaran:*")
              expenseByCategory.forEach { (cat, amount) ->
                val pct = if (totalExpense > 0) (amount / totalExpense * 100).toInt() else 0
                appendLine("- $cat: ${currencyFormat.format(amount)} ($pct%)")
              }
              appendLine("\nDicatat via Aplikasi RantauSmart 🎓")
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, reportText)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Keuangan"))
          },
          modifier = Modifier.testTag("share_summary_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Laporan", fontSize = 12.sp)
        }
      }
    }

    item { Spacer(modifier = Modifier.height(14.dp)) }

    // Period Tabs
    item {
      val tabs = listOf(
        SummaryPeriod.TODAY to "Hari Ini",
        SummaryPeriod.THIS_WEEK to "Minggu Ini",
        SummaryPeriod.THIS_MONTH to "Bulan Ini"
      )
      TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedPeriod },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
      ) {
        tabs.forEachIndexed { index, (period, label) ->
          val isSelected = selectedPeriod == period
          Tab(
            selected = isSelected,
            onClick = { viewModel.setSummaryPeriod(period) },
            text = {
              Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
            }
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }

    // Cashflow In vs Out Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "ARUS KAS",
            style = MaterialTheme.typography.labelSmall.copy(
              letterSpacing = 1.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Pemasukan
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MintAccent.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MintAccent, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pemasukan", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = currencyFormat.format(totalIncome).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MintAccent
                )
              )
            }

            // Pengeluaran
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CoralExpense.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = CoralExpense, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pengeluaran", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = currencyFormat.format(totalExpense).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = CoralExpense
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Net Difference Bar
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (netSavings >= 0) "Surplus Tabungan:" else "Defisit Kas:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = currencyFormat.format(netSavings).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = if (netSavings >= 0) MintAccent else CoralExpense
                )
              )
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(18.dp)) }

    // Category Breakdown Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          Icons.Default.PieChart,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Distribusi Pengeluaran per Kategori",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    item { Spacer(modifier = Modifier.height(10.dp)) }

    if (expenseByCategory.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Tidak ada pengeluaran pada periode ini.",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            )
          }
        }
      }
    } else {
      items(expenseByCategory) { (category, amount) ->
        val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
        val animatedPct by animateFloatAsState(
          targetValue = pct,
          animationSpec = tween(500),
          label = "cat_pct"
        )

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = getCategoryIcon(category, true),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = category,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = currencyFormat.format(amount).replace("Rp", "Rp "),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CoralExpense
                  )
                )
                Text(
                  text = "${(pct * 100).toInt()}% dari total",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
              progress = { animatedPct },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = EmeraldPrimary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(18.dp)) }

    // Smart Student Saving Tips Card
    item {
      val topCategory = expenseByCategory.firstOrNull()?.first
      val tipText = when (topCategory) {
        Categories.MAKANAN -> "💡 Pengeluaran terbesarmu ada di Makanan. Tips: Masak nasi sendiri di magic com kosan dan beli lauk sayur di warteg bisa menghemat hingga Rp 350.000 sebulan!"
        Categories.TEMPAT_TINGGAL -> "💡 Iuran kos mengambil porsi utama. Tips: Sisihkan jatah sewa kos segera saat uang bulanan baru cair di awal bulan agar tidak terpakai jajan."
        Categories.TRANSPORTASI -> "💡 Biaya transportasi tinggi. Tips: Buat jadwal tebengan kampus bersama teman satu kosan atau gunakan kartu langganan transportasi umum bila ada."
        Categories.TAGIHAN -> "💡 Tagihan pulsa/wifi cukup besar. Tips: Manfaatkan wifi gratis di perpustakaan atau area kampus untuk download materi kuliah besar."
        else -> "💡 Aturan 50-30-20 untuk Anak Rantau: 50% untuk kebutuhan pokok (makan, kos), 30% untuk jajan santai/kuota, dan 20% tabungan darurat bila jatuh sakit!"
      }

      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = AmberWarning,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "TIPS HEMAT MAHASISWA RANTAU",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AmberWarning,
                letterSpacing = 0.5.sp
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = tipText,
              style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
          }
        }
      }
    }
  }
}
