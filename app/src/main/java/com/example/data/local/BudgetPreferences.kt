package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("rantau_smart_prefs", Context.MODE_PRIVATE)

  private val _dailyBudgetLimit = MutableStateFlow(
    prefs.getFloat(KEY_DAILY_BUDGET, DEFAULT_DAILY_BUDGET).toDouble()
  )
  val dailyBudgetLimit: StateFlow<Double> = _dailyBudgetLimit.asStateFlow()

  fun setDailyBudgetLimit(limit: Double) {
    prefs.edit().putFloat(KEY_DAILY_BUDGET, limit.toFloat()).apply()
    _dailyBudgetLimit.value = limit
  }

  companion object {
    private const val KEY_DAILY_BUDGET = "key_daily_budget_limit"
    const val DEFAULT_DAILY_BUDGET = 50_000f // Rp 50.000 default
  }
}
