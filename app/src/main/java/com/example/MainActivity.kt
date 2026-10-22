package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.EditBudgetDialog
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        RantauSmartApp()
      }
    }
  }
}

@Composable
fun RantauSmartApp(
  viewModel: MainViewModel = viewModel()
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val showAddDialog by viewModel.showAddDialog.collectAsState()
  val showBudgetDialog by viewModel.showBudgetDialog.collectAsState()
  val dailyLimit by viewModel.dailyBudgetLimit.collectAsState()

  // Handle system back navigation to return to Home when on sub-screens
  BackHandler(enabled = currentScreen != AppScreen.HOME) {
    viewModel.navigateTo(AppScreen.HOME)
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      BottomNavBar(
        currentScreen = currentScreen,
        onScreenSelected = { screen -> viewModel.navigateTo(screen) }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.HOME -> HomeScreen(viewModel = viewModel)
        AppScreen.AI_CHAT -> AiChatScreen(viewModel = viewModel)
        AppScreen.SUMMARY -> SummaryScreen(viewModel = viewModel)
      }
    }
  }

  // Modals
  if (showAddDialog) {
    AddTransactionDialog(
      onDismiss = { viewModel.showAddDialog.value = false },
      onSave = { type, amount, category, note ->
        viewModel.addManualTransaction(type, amount, category, note)
      }
    )
  }

  if (showBudgetDialog) {
    EditBudgetDialog(
      currentLimit = dailyLimit,
      onDismiss = { viewModel.showBudgetDialog.value = false },
      onSave = { newLimit ->
        viewModel.updateDailyBudget(newLimit)
      }
    )
  }
}
