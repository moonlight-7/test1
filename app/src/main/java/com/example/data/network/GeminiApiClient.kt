package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Categories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedTransactionResult(
  val isTransaction: Boolean,
  val type: String, // "EXPENSE" or "INCOME"
  val amount: Double,
  val category: String,
  val note: String,
  val replyMessage: String
)

class GeminiApiClient {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun parseUserMessage(userMessage: String): ParsedTransactionResult = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val geminiResult = callGeminiRestApi(apiKey, userMessage)
        if (geminiResult != null) {
          return@withContext geminiResult
        }
      } catch (e: Exception) {
        Log.e("GeminiApiClient", "Gemini API call failed, falling back to local NLP", e)
      }
    }

    // Local Indonesian NLP Parser Fallback
    return@withContext parseIndonesianLocalNlp(userMessage)
  }

  private fun callGeminiRestApi(apiKey: String, message: String): ParsedTransactionResult? {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

    val systemPrompt = """
      Kamu adalah asisten keuangan pintar untuk mahasiswa rantau bernama RantauSmart.
      Tugasmu adalah menganalisis pesan pengguna tentang pemasukan atau pengeluaran keuangan dan mengekstrak datanya ke format JSON.
      
      PENTING: Keluarkan HANYA satu objek JSON valid tanpa markdown formatting dengan struktur:
      {
        "isTransaction": boolean, // true jika pesan berkaitan dengan pengeluaran atau pemasukan uang
        "type": "EXPENSE" | "INCOME", // "EXPENSE" jika belanja/biaya/keluar, "INCOME" jika uang masuk/gaji/kiriman
        "amount": number, // nominal angka dalam Rupiah murni, misal 18000 (jika '18k' artinya 18000, '1.5jt' artinya 1500000)
        "category": string, // salah satu dari: "Makanan & Minuman", "Kos & Tempat Tinggal", "Transportasi & Bensin", "Kuliah & Pendidikan", "Kebutuhan Kos & Mandi", "Hiburan & Nongkrong", "Pulsa, Wifi & Listrik", "Uang Saku / Kiriman Ortu", "Gaji / Pekerjaan Part-time", "Lain-lain"
        "note": string, // ringkasan singkat barang/kegiatan yang dicatat
        "replyMessage": string // pesan ramah, suportif, dan singkat khas sahabat anak kos dalam bahasa Indonesia
      }
    """.trimIndent()

    val requestJson = JSONObject().apply {
      val contentsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().put("text", message))
          })
        })
      }
      put("contents", contentsArray)

      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().put("text", systemPrompt))
        })
      })

      put("generationConfig", JSONObject().apply {
        put("temperature", 0.2)
        put("responseMimeType", "application/json")
      })
    }

    val body = requestJson.toString().toRequestBody(jsonMediaType)
    val request = Request.Builder()
      .url(url)
      .post(body)
      .build()

    val response = okHttpClient.newCall(request).execute()
    if (!response.isSuccessful) {
      Log.w("GeminiApiClient", "API returned code ${response.code}: ${response.body?.string()}")
      return null
    }

    val responseBody = response.body?.string() ?: return null
    val rootObj = JSONObject(responseBody)
    val candidates = rootObj.optJSONArray("candidates") ?: return null
    if (candidates.length() == 0) return null

    val firstCandidate = candidates.getJSONObject(0)
    val content = firstCandidate.optJSONObject("content") ?: return null
    val parts = content.optJSONArray("parts") ?: return null
    if (parts.length() == 0) return null

    val rawText = parts.getJSONObject(0).optString("text", "")
    val cleanJsonText = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

    val parsed = JSONObject(cleanJsonText)
    val isTransaction = parsed.optBoolean("isTransaction", true)
    val type = parsed.optString("type", "EXPENSE").uppercase()
    val amount = parsed.optDouble("amount", 0.0)
    val category = parsed.optString("category", Categories.LAINNYA)
    val note = parsed.optString("note", message)
    val replyMessage = parsed.optString("replyMessage", "Transaksi berhasil dicatat oleh AI!")

    return ParsedTransactionResult(
      isTransaction = isTransaction,
      type = if (type == "INCOME") "INCOME" else "EXPENSE",
      amount = amount,
      category = category,
      note = note,
      replyMessage = replyMessage
    )
  }

  /**
   * Local Indonesian NLP fallback that accurately recognizes Indonesian student expressions:
   * e.g. "makan siang 15k", "beli sabun 25000", "dapat kiriman 1jt", "bensin 20rb"
   */
  fun parseIndonesianLocalNlp(text: String): ParsedTransactionResult {
    val lower = text.lowercase().trim()

    // Detect Income indicators
    val isIncome = lower.contains("dapat") || lower.contains("kiriman") ||
      lower.contains("gaji") || lower.contains("transfer dari") ||
      lower.contains("dikasih") || lower.contains("uang saku") ||
      lower.contains("pemasukan") || lower.contains("cair")

    val type = if (isIncome) "INCOME" else "EXPENSE"

    // Extract amount
    val amount = extractAmountFromText(lower)

    // Determine category
    val category = if (type == "INCOME") {
      when {
        lower.contains("gaji") || lower.contains("kerja") || lower.contains("part time") -> "Gaji / Pekerjaan Part-time"
        lower.contains("beasiswa") -> "Beasiswa"
        else -> "Uang Saku / Kiriman Ortu"
      }
    } else {
      when {
        lower.contains("makan") || lower.contains("minum") || lower.contains("kopi") ||
          lower.contains("warteg") || lower.contains("nasi") || lower.contains("ayam") ||
          lower.contains("jajan") || lower.contains("snack") || lower.contains("sarapan") ||
          lower.contains("bakso") || lower.contains("mie") || lower.contains("resto") -> Categories.MAKANAN

        lower.contains("kos") || lower.contains("kost") || lower.contains("kontrakan") ||
          lower.contains("sewa kamar") || lower.contains("laundry") -> Categories.TEMPAT_TINGGAL

        lower.contains("bensin") || lower.contains("ojol") || lower.contains("gojek") ||
          lower.contains("grab") || lower.contains("angkot") || lower.contains("bus") ||
          lower.contains("parkir") || lower.contains("service") || lower.contains("oli") -> Categories.TRANSPORTASI

        lower.contains("buku") || lower.contains("fotokopi") || lower.contains("print") ||
          lower.contains("kuliah") || lower.contains("ukt") || lower.contains("skripsi") ||
          lower.contains("alat tulis") || lower.contains("pulpen") -> Categories.KULIAH

        lower.contains("sabun") || lower.contains("sampo") || lower.contains("detergen") ||
          lower.contains("odol") || lower.contains("galon") || lower.contains("air isi ulang") ||
          lower.contains("tisu") || lower.contains("pasta gigi") -> Categories.KEBUTUHAN_HARIAN

        lower.contains("nongkrong") || lower.contains("game") || lower.contains("bioskop") ||
          lower.contains("steam") || lower.contains("nonton") || lower.contains("jalan") -> Categories.HIBURAN

        lower.contains("pulsa") || lower.contains("kuota") || lower.contains("wifi") ||
          lower.contains("listrik") || lower.contains("token") -> Categories.TAGIHAN

        else -> Categories.LAINNYA
      }
    }

    // Determine clean note
    val note = text.replace(Regex("""(?i)\b(\d+[\d.,]*\s*(?:k|rb|ribu|jt|juta)?)\b"""), "").trim().let {
      if (it.isBlank()) text else it
    }

    val reply = if (type == "INCOME") {
      "Alhamdulillah, uang masuk sebesar Rp ${amount.toLong()} berhasil dicatat ke saldo!"
    } else {
      "Pengeluaran sebesar Rp ${amount.toLong()} untuk $category berhasil dicatat otomatis."
    }

    return ParsedTransactionResult(
      isTransaction = amount > 0,
      type = type,
      amount = if (amount > 0) amount else 10000.0,
      category = category,
      note = note,
      replyMessage = reply
    )
  }

  private fun extractAmountFromText(text: String): Double {
    // Match patterns like "1.5jt", "1,5 juta", "20k", "25rb", "50.000", "50000"
    val jtPattern = Pattern.compile("""(\d+(?:[.,]\d+)?)\s*(?:jt|juta)""")
    val jtMatcher = jtPattern.matcher(text)
    if (jtMatcher.find()) {
      val numStr = jtMatcher.group(1)?.replace(",", ".") ?: "1"
      return (numStr.toDoubleOrNull() ?: 1.0) * 1_000_000.0
    }

    val kPattern = Pattern.compile("""(\d+(?:[.,]\d+)?)\s*(?:k|rb|ribu)""")
    val kMatcher = kPattern.matcher(text)
    if (kMatcher.find()) {
      val numStr = kMatcher.group(1)?.replace(",", ".") ?: "1"
      return (numStr.toDoubleOrNull() ?: 1.0) * 1_000.0
    }

    val plainNumPattern = Pattern.compile("""\b(?:rp\.?\s*)?(\d{1,3}(?:\.\d{3})+|\d+)\b""")
    val plainMatcher = plainNumPattern.matcher(text)
    if (plainMatcher.find()) {
      val numStr = plainMatcher.group(1)?.replace(".", "") ?: "0"
      val parsed = numStr.toDoubleOrNull() ?: 0.0
      if (parsed > 0) return parsed
    }

    return 0.0
  }
}
