package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.AppScreen

@Composable
fun BottomNavBar(
  currentScreen: AppScreen,
  onScreenSelected: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    modifier = modifier.navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = currentScreen == AppScreen.HOME,
      onClick = { onScreenSelected(AppScreen.HOME) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
          contentDescription = "Beranda"
        )
      },
      label = {
        Text(
          "Beranda",
          fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
        )
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = EmeraldPrimary,
        selectedTextColor = EmeraldPrimary
      ),
      modifier = Modifier.testTag("nav_item_home")
    )

    NavigationBarItem(
      selected = currentScreen == AppScreen.AI_CHAT,
      onClick = { onScreenSelected(AppScreen.AI_CHAT) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.AI_CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
          contentDescription = "Chat AI"
        )
      },
      label = {
        Text(
          "Chat AI",
          fontWeight = if (currentScreen == AppScreen.AI_CHAT) FontWeight.Bold else FontWeight.Normal
        )
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = EmeraldPrimary,
        selectedTextColor = EmeraldPrimary
      ),
      modifier = Modifier.testTag("nav_item_ai_chat")
    )

    NavigationBarItem(
      selected = currentScreen == AppScreen.SUMMARY,
      onClick = { onScreenSelected(AppScreen.SUMMARY) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.SUMMARY) Icons.Filled.PieChart else Icons.Outlined.PieChartOutline,
          contentDescription = "Ringkasan"
        )
      },
      label = {
        Text(
          "Ringkasan",
          fontWeight = if (currentScreen == AppScreen.SUMMARY) FontWeight.Bold else FontWeight.Normal
        )
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = EmeraldPrimary,
        selectedTextColor = EmeraldPrimary
      ),
      modifier = Modifier.testTag("nav_item_summary")
    )
  }
}
