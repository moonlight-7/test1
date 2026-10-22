package com.example

import com.example.data.model.Categories
import com.example.data.network.GeminiApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  private val parser = GeminiApiClient()

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testIndonesianNlpParser_expenseWithK() {
    val result = parser.parseIndonesianLocalNlp("Makan siang warteg 18k")
    assertTrue(result.isTransaction)
    assertEquals("EXPENSE", result.type)
    assertEquals(18000.0, result.amount, 0.01)
    assertEquals(Categories.MAKANAN, result.category)
  }

  @Test
  fun testIndonesianNlpParser_expenseWithRb() {
    val result = parser.parseIndonesianLocalNlp("Beli sabun mandi 25rb")
    assertTrue(result.isTransaction)
    assertEquals("EXPENSE", result.type)
    assertEquals(25000.0, result.amount, 0.01)
    assertEquals(Categories.KEBUTUHAN_HARIAN, result.category)
  }

  @Test
  fun testIndonesianNlpParser_incomeWithJt() {
    val result = parser.parseIndonesianLocalNlp("Dapat kiriman uang saku 1.5jt dari ortu")
    assertTrue(result.isTransaction)
    assertEquals("INCOME", result.type)
    assertEquals(1500000.0, result.amount, 0.01)
  }
}
