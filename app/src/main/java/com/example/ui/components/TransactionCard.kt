package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.TransactionEntity
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.MintAccent

@Composable
fun TransactionCard(
  transaction: TransactionEntity,
  onDeleteClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isExpense = transaction.isExpense
  val icon = getCategoryIcon(transaction.category, isExpense)
  val categoryBgColor = if (isExpense) {
    CoralExpense.copy(alpha = 0.1f)
  } else {
    MintAccent.copy(alpha = 0.12f)
  }
  val categoryIconTint = if (isExpense) CoralExpense else MintAccent

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("transaction_card_${transaction.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Category Icon Bubble
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(categoryBgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = transaction.category,
            tint = categoryIconTint,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title, Category, Source Tag & Timestamp
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = transaction.note,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )

            if (transaction.source == "AI_CHAT") {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Dicatat via AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "AI Chat",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Text(
              text = transaction.category,
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            )
            Text(
              text = " • ${transaction.formattedTime}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Amount & Delete Action
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${if (isExpense) "-" else "+"}${transaction.formattedAmount}",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isExpense) CoralExpense else MintAccent
            )
          )
          Text(
            text = transaction.formattedDate,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
          )
        }

        if (onDeleteClick != null) {
          IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_transaction_${transaction.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Hapus Transaksi",
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

fun getCategoryIcon(category: String, isExpense: Boolean): ImageVector {
  if (!isExpense) return Icons.Default.LocalAtm
  return when (category) {
    Categories.MAKANAN -> Icons.Default.Restaurant
    Categories.TEMPAT_TINGGAL -> Icons.Default.Home
    Categories.TRANSPORTASI -> Icons.Default.DirectionsCar
    Categories.KULIAH -> Icons.Default.School
    Categories.KEBUTUHAN_HARIAN -> Icons.Default.ShoppingCart
    Categories.HIBURAN -> Icons.Default.SportsEsports
    Categories.TAGIHAN -> Icons.Default.Wifi
    else -> Icons.Default.LocalAtm
  }
}
