package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.SenderType
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AiChatScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val chatMessages by viewModel.chatMessages.collectAsState()
  val isAiThinking by viewModel.isAiThinking.collectAsState()
  val dailyLimit by viewModel.dailyBudgetLimit.collectAsState()
  val todaySpent by viewModel.todaySpent.collectAsState()

  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom on new message
  LaunchedEffect(chatMessages.size, isAiThinking) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  val quickPrompts = listOf(
    "Makan siang warteg 18k",
    "Beli sabun & detergen 25rb",
    "Bensin motor 20.000",
    "Dikirimi uang bulanan 1.5jt",
    "Fotokopi materi kuliah 10k",
    "Iuran wifi kos 50.000"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .imePadding()
      .testTag("ai_chat_screen")
  ) {
    // Header Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.SmartToy,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Catat via Chat AI",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = "Ketik bebas, AI langsung simpan otomatis",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldDark.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Gemini 3.5 Flash",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
          }
        }
      }
    }

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(chatMessages, key = { it.id }) { msg ->
        ChatMessageBubble(
          message = msg,
          onUndoClick = { viewModel.undoChatMessageTransaction(msg) }
        )
      }

      if (isAiThinking) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = EmeraldPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "AI sedang membaca & menyimpan transaksi...",
              style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
            )
          }
        }
      }
    }

    // Quick Prompt Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(quickPrompts) { prompt ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
              inputText = prompt
              viewModel.sendChatMessage(prompt)
              inputText = ""
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    }

    // Input Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Ketik transaksi (misal: warteg 18k)...") },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_field"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank() && !isAiThinking) {
                viewModel.sendChatMessage(inputText)
                inputText = ""
              }
            }
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (inputText.isNotBlank() && !isAiThinking) {
              viewModel.sendChatMessage(inputText)
              inputText = ""
            }
          },
          enabled = inputText.isNotBlank() && !isAiThinking,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (inputText.isNotBlank() && !isAiThinking) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
            .testTag("send_chat_button")
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Kirim Pesan",
            tint = if (inputText.isNotBlank() && !isAiThinking) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onUndoClick: () -> Unit
) {
  val isUser = message.sender == SenderType.USER

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Card(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) EmeraldPrimary else MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.82f else 0.92f)
        .testTag("chat_message_${message.id}")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
          )
        )

        // If this AI message auto-saved a transaction
        message.transaction?.let { tx ->
          Spacer(modifier = Modifier.height(10.dp))
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (tx.isExpense) CoralExpense.copy(alpha = 0.08f) else MintAccent.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (tx.isExpense) CoralExpense else MintAccent,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (tx.isExpense) "Pengeluaran Tersimpan" else "Pemasukan Tersimpan",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (tx.isExpense) CoralExpense else MintAccent
                    )
                  )
                }

                Text(
                  text = "${if (tx.isExpense) "-" else "+"}${tx.formattedAmount}",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (tx.isExpense) CoralExpense else MintAccent
                  )
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = getCategoryIcon(tx.category, tx.isExpense),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${tx.category} • \"${tx.note}\"",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                  )
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Undo Button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onUndoClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("undo_transaction_${tx.id}")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Batal",
                      tint = CoralExpense,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Batal / Hapus",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralExpense
                      )
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = message.formattedTime,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
          ),
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}
