package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val type: String, // "EXPENSE" or "INCOME"
  val amount: Double,
  val category: String,
  val note: String,
  val timestamp: Long = System.currentTimeMillis(),
  val source: String = "MANUAL" // "MANUAL" or "AI_CHAT"
) {
  val isExpense: Boolean
    get() = type.equals("EXPENSE", ignoreCase = true)

  val formattedAmount: String
    get() {
      val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
      formatter.maximumFractionDigits = 0
      return formatter.format(amount).replace("Rp", "Rp ")
    }

  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
      return sdf.format(Date(timestamp))
    }

  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
      return sdf.format(Date(timestamp))
    }
}

object Categories {
  const val MAKANAN = "Makanan & Minuman"
  const val TEMPAT_TINGGAL = "Kos & Tempat Tinggal"
  const val TRANSPORTASI = "Transportasi & Bensin"
  const val KULIAH = "Kuliah & Pendidikan"
  const val KEBUTUHAN_HARIAN = "Kebutuhan Kos & Mandi"
  const val HIBURAN = "Hiburan & Nongkrong"
  const val TAGIHAN = "Pulsa, Wifi & Listrik"
  const val LAINNYA = "Lain-lain"

  val allExpenseCategories = listOf(
    MAKANAN,
    TEMPAT_TINGGAL,
    TRANSPORTASI,
    KULIAH,
    KEBUTUHAN_HARIAN,
    HIBURAN,
    TAGIHAN,
    LAINNYA
  )

  val allIncomeCategories = listOf(
    "Uang Saku / Kiriman Ortu",
    "Gaji / Pekerjaan Part-time",
    "Beasiswa",
    "Freelance / Jualan",
    "Lain-lain"
  )
}
