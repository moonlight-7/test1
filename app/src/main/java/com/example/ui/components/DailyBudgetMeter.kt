package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.BudgetHealth
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DailyBudgetMeter(
  todaySpent: Double,
  dailyLimit: Double,
  health: BudgetHealth,
  onEditBudgetClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
  }

  val progress = if (dailyLimit > 0) {
    (todaySpent / dailyLimit).toFloat().coerceIn(0f, 1.2f)
  } else 0f

  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceAtMost(1f),
    animationSpec = tween(durationMillis = 600),
    label = "progress"
  )

  val statusColor by animateColorAsState(
    targetValue = when (health) {
      BudgetHealth.SAFE -> MintAccent
      BudgetHealth.WARNING -> AmberWarning
      BudgetHealth.EXCEEDED -> CoralExpense
    },
    label = "statusColor"
  )

  val remaining = (dailyLimit - todaySpent).coerceAtLeast(0.0)
  val isOverbudget = todaySpent > dailyLimit

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("daily_budget_meter_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header with Edit Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "TARGET ANGGARAN HARIAN",
            style = MaterialTheme.typography.labelMedium.copy(
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = "Batas: ${currencyFormat.format(dailyLimit).replace("Rp", "Rp ")} / hari",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable(onClick = onEditBudgetClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("edit_daily_budget_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Ubah Batas Harian",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Atur",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Today's Spent vs Remaining Display
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "Terpakai Hari Ini",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          )
          Text(
            text = currencyFormat.format(todaySpent).replace("Rp", "Rp "),
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = if (isOverbudget) CoralExpense else MaterialTheme.colorScheme.onSurface
            )
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (isOverbudget) "Melebihi Batas" else "Sisa Jatah",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          )
          Text(
            text = if (isOverbudget) {
              "+${currencyFormat.format(todaySpent - dailyLimit).replace("Rp", "Rp ")}"
            } else {
              currencyFormat.format(remaining).replace("Rp", "Rp ")
            },
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = statusColor
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Visual Progress Bar
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp)),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Status Indicator Banner
      val (bannerBg, bannerIcon, bannerText) = when (health) {
        BudgetHealth.SAFE -> Triple(
          MintAccent.copy(alpha = 0.12f),
          Icons.Default.Info,
          "Pengeluaran aman! Kuota belanja hari ini masih banyak."
        )
        BudgetHealth.WARNING -> Triple(
          AmberWarning.copy(alpha = 0.15f),
          Icons.Default.Warning,
          "Hati-hati! Pengeluaran sudah mencapai 80% batas harian."
        )
        BudgetHealth.EXCEEDED -> Triple(
          CoralExpense.copy(alpha = 0.15f),
          Icons.Default.Warning,
          "Peringatan: Kamu telah melampaui batas anggaran hari ini!"
        )
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(bannerBg)
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = bannerIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = bannerText,
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Medium,
              color = if (health == BudgetHealth.SAFE) EmeraldDark else statusColor
            )
          )
        }
      }
    }
  }
}
