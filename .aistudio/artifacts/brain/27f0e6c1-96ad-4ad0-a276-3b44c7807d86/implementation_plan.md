# RantauSmart: Catat Keuanganmu Dengan Smart

Aplikasi manajemen finansial cerdas yang dirancang khusus untuk mahasiswa rantau, memudahkan pencatatan pengeluaran dan pemasukan harian secara instan melalui percakapan AI, monitoring batas anggaran harian manual, serta ringkasan visual arus kas yang rapi dan transparan.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan sesi tanya jawab di Tahap 1, keputusan-keputusan kunci berikut telah disepakati dan diintegrasikan ke dalam arsitektur aplikasi:

- **Identitas & Nama Aplikasi**: **RantauSmart: Catat Keuanganmu Dengan Smart** (sesuai pilihan pengguna).
- **Alur Interaksi Chat AI**: **Auto-Save Langsung** — ketika pengguna mengirimkan pesan bahasa santai/gaul (misal: *"Tadi makan siang warteg 18k sama es teh 3k"* atau *"Dikirimi uang bulanan dari mamah 1.500.000"*), AI (Gemini 3.5 Flash) mengekstrak nominal, tipe transaksi (pemasukan/pengeluaran), kategori, dan keterangan, langsung menyimpannya ke database secara otomatis, lalu membalas dengan ringkasan status jatah harian terkini.
- **Sistem Anggaran Harian**: **Batas Nominal Manual** — pengguna dapat mengatur target batas pengeluaran harian (misal: Rp 50.000/hari). Aplikasi menghitung akumulasi belanja hari ini, sisa kuota jatah hari ini, serta memberikan indikator visual jelas (*Aman*, *Hati-hati*, atau *Overbudget*).
- **Penyimpanan Data (Offline-First Local Storage)**: Menggunakan **Room Database** (didukung KSP & Flow) untuk pencatatan instan tanpa perlu repot login akun di kosan, menjaga privasi data finansial, dengan arsitektur Repository yang siap ditingkatkan ke Cloud Firestore jika diperlukan di masa depan.

---

### 1. Overview & Core Concept

- **What It Does**:
  RantauSmart adalah asisten keuangan pribadi saku bagi mahasiswa yang hidup mandiri jauh dari orang tua. Aplikasi ini menghilangkan beban mencatat bon manual yang melelahkan dengan menyediakan kolom chat AI alami, pelacak batas pengeluaran harian yang proaktif, serta dashboard grafik ringkasan keuangan mingguan/bulanan.
- **Target Audience / Persona**:
  Mahasiswa rantau dan anak kos yang memiliki jatah uang saku terbatas, sering lupa mencatat pengeluaran kecil (beli galon, laundry, jajan cilok, fotokopi materi kuliah), dan rawan kehabisan uang sebelum akhir bulan.
- **Key Value**:
  Pencatatan dalam 3 detik tanpa form panjang yang rumit, kendali anggaran harian yang ketat agar tidak "kanker" (kantong kering) di akhir bulan, dan rekapitulasi pengeluaran per kategori yang mudah dipahami.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Flow 1: Beranda & Monitoring Anggaran Harian**:
   - Pengguna membuka aplikasi dan langsung melihat status anggaran hari ini: Donut/Linear Progress Bar interaktif menunjukkan pengeluaran hari ini vs batas harian (misal: "Rp 32.000 / Rp 50.000"), sisa jatah ("Sisa Rp 18.000"), dan kartu ringkasan saldo total.
   - Quick action bar: tombol cepat untuk "Tambah Manual" atau "Buka AI Chat".
   - Daftar transaksi hari ini dan transaksi terkini dengan pengelompokan tanggal yang rapi.
2. **Flow 2: Pencatatan Kilat via AI Chat**:
   - Tab navigasi kedua membuka ruang obrolan cerdas.
   - Pengguna mengetik atau memilih chip quick prompt (misal: *"Beli detergen & sabun 25rb"*, *"Bayar wifi kos 75.000"*, *"Habis servis motor ganti oli 65k"*).
   - Gemini 3.5 Flash mengekstrak entri transaksi secara terstruktur (JSON), menyimpannya langsung ke database lokal, dan merespons dalam obrolan dengan kartu transaksi rapi (*"✅ Tersimpan! Pengeluaran: Rp 28.000 (Kategori: Makanan & Minuman). Sisa batas harianmu tinggal Rp 22.000. Tetap hemat ya!"*).
   - Tombol undo / edit instan tersedia di bubble chat jika pengguna ingin mengoreksi nominal.
3. **Flow 3: Halaman Analisis & Ringkasan Keuangan**:
   - Filter rentang waktu: Hari Ini, Minggu Ini, Bulan Ini.
   - Perbandingan total pemasukan vs pengeluaran, rasio hemat, dan diagram pengeluaran per kategori (Makanan, Tempat Tinggal/Kos, Transportasi, Kuliah & Tugas, Hiburan, Kebutuhan Harian, Lainnya).
4. **Flow 4: Pengaturan Anggaran & Kategori**:
   - Dialog/Sheet pengaturan untuk mengubah batas nominal harian sesuai kemampuan mahasiswa.
   - Reset data atau ekspor ringkasan teks.

#### Visual Identity & Theme
- **Aesthetic Direction**: *Fresh Modern Editorial & Neo-Fintech* — bersih, ramah kantong mahasiswa, terstruktur, bebas dari kesan membosankan software akuntansi kaku.
- **Color Palette**:
  - Primary: Deep Emerald Forest (`#005A43` / `#008763`) — merepresentasikan stabilitas finansial dan ketenangan.
  - Secondary / Accent: Energetic Mint & Warm Amber (`#00C988` untuk pemasukan/status aman, `#FFAB00` untuk peringatan, `#E53935` untuk pengeluaran/overbudget).
  - Surface & Background: Clean Off-White Warm (`#F8FAF8`) dan Light Container Crisp Surface (`#FFFFFF` dengan outline halus `#E2E8F0`).
  - Dark Theme Ready: Slate Dark (`#121816`) dengan kontras tinggi untuk pemakaian malam hari di kamar kos.
- **Typography & Hierarchy**:
  - Angka Saldo & Nominal: Bold Modern Monospace/Sans berukuran besar (`HeadlineMedium`/`TitleLarge`) dengan pemisah ribuan Rupiah (`Rp 1.500.000`).
  - Label Kategori: Chip pill dengan ikon kategori yang ekspresif.
- **Interactive Feedback**:
  - Haptic touch feedback & M3 ripple pada setiap tombol aksi.
  - Animasi transisi halus (`AnimatedVisibility`) saat transaksi baru tersimpan di chat.
  - Banner peringatan dinamis ketika pengeluaran melampaui 80% atau 100% dari target harian.

---

### 3. Key Product Decisions & Trade-Offs

#### Keputusan 1: Alur Simpan Otomatis (Direct Auto-Save) pada Chat AI
- **Pendekatan Terpilih**: Mengirim instruksi sistem ke model `gemini-3.5-flash` dengan `responseSchema` JSON terstruktur. Setelah AI merespons, transaksi langsung di-insert ke Room Database tanpa popup modal tambahan, lalu bubble respons AI menampilkan rincian yang tersimpan beserta tombol "Batal / Hapus" darurat.
- **Alasan**: Sesuai jawaban preferensi pengguna di Tahap 1. Mahasiswa rantau butuh kecepatan mencatat di sela aktivitas kampus tanpa harus menekan tombol konfirmasi berulang kali.
- **Alternatif yang Dipertimbangkan**: Dialog konfirmasi sebelum simpan ditolak karena menambah friksi dan memperlambat alur pencatatan harian.

#### Keputusan 2: Penyimpanan Lokal Room Database (Offline-First)
- **Pendekatan Terpilih**: Menggunakan Room Database lokal bawaan Android dengan Kotlin Coroutines & Flow.
- **Alasan**: Mahasiswa di kos sering kali memiliki koneksi internet yang fluktuatif atau kuota terbatas. Transaksi dan monitoring harian dapat diakses 100% secara instan dan offline, sementara fitur chat AI memanfaatkan koneksi internet saat mengirim prompt ke Gemini REST API.
- **Alternatif yang Dipertimbangkan**: Firebase Firestore memerlukan Google Sign-In wajib di awal aplikasi, yang menambah kendala login sebelum pengguna bisa mulai mencatat.

#### Keputusan 3: Penentuan Batas Harian Manual
- **Pendekatan Terpilih**: Pengguna menentukan nominal pasti target harian (misal: Rp 40.000 atau Rp 50.000). Aplikasi menyimpannya secara persisten dan menghitung secara real-time total pengeluaran dari jam 00:00 hingga 23:59 setiap hari.
- **Alasan**: Memberikan kendali penuh dan rasa disiplin yang nyata bagi mahasiswa yang sudah membagi uang sakunya dalam jatah amplop harian.

---

### 4. Technical Architecture & Data Strategy

#### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          UI Layer (Jetpack Compose)                    │
│                                                                        │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │   HomeScreen          │ │   AiChatScreen        │ │ SummaryScreen │ │
│  │  - Daily Budget Bar   │ │  - Conversation List  │ │ - Cashflow    │ │
│  │  - Today Summary      │ │  - Auto-Parse Chat    │ │ - Categories  │ │
│  │  - Recent Transactions│ │  - Undo Action Chip   │ │ - Trend Chart │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └───────┬───────┘ │
│              │                         │                     │         │
│              └────────────────┐        │       ┌─────────────┘         │
│                               ▼        ▼       ▼                       │
│                  ┌────────────────────────────────────────┐            │
│                  │           MainViewModel                │            │
│                  │  - UI State (Budget, Filter, Status)   │            │
│                  │  - Transaction StateFlow               │            │
│                  │  - Chat Messages StateFlow             │            │
│                  └────────────────────┬───────────────────┘            │
└───────────────────────────────────────┼────────────────────────────────┘
                                        │
┌───────────────────────────────────────┼────────────────────────────────┐
│ Data & Service Layer                  │                                │
│                                       ▼                                │
│      ┌─────────────────────────────────────────────────┐               │
│      │               TransactionRepository             │               │
│      └────────┬───────────────────────────────┬────────┘               │
│               │                               │                        │
│               ▼                               ▼                        │
│   ┌────────────────────────┐      ┌─────────────────────────┐          │
│   │   Room Database (Local)│      │  Gemini REST Client     │          │
│   │  - TransactionEntity   │      │  - Gemini 3.5 Flash     │          │
│   │  - BudgetSettingsStore │      │  - JSON Structured Spec │          │
│   └────────────────────────┘      └─────────────────────────┘          │
└────────────────────────────────────────────────────────────────────────┘
```

#### Data Model Entities

1. **TransactionEntity** (`room` table: `transactions`):
   - `id: Long` (Primary Key, autoGenerate = true)
   - `type: String` ("EXPENSE" / "INCOME")
   - `amount: Double` (nominal rupiah)
   - `category: String` (e.g., "Makanan & Minuman", "Tempat Tinggal", "Transportasi", "Kuliah", "Kebutuhan Harian", "Hiburan", "Lainnya")
   - `note: String` (keterangan, misal "Nasi Padang + Es Teh")
   - `timestamp: Long` (waktu pencatatan dalam milidetik)
   - `source: String` ("AI_CHAT" atau "MANUAL")

2. **BudgetSettings**:
   - `dailyBudgetLimit: Double` (default: Rp 50.000)
   - `currencySymbol: String` (default: "Rp")

3. **ChatMessage**:
   - `id: String`
   - `sender: SenderType` (`USER` / `AI`)
   - `text: String`
   - `associatedTransactionId: Long?` (untuk aksi undo/hapus instan dari chat)
   - `timestamp: Long`

#### Gemini Structured Parsing Schema
Model: `gemini-3.5-flash` via Direct REST API (didukung `BuildConfig.GEMINI_API_KEY`).
Prompt instruksi sistem bahasa Indonesia:
*"Kamu adalah asisten keuangan anak rantau RantauSmart. Tugasmu mengekstrak transaksi pemasukan atau pengeluaran dari pesan pengguna ke format JSON berikut: { type: 'EXPENSE'|'INCOME', amount: number, category: string, note: string, replyMessage: string }."*

---

### Implementation Phasing & Validation Plan

1. **Setup & Dependencies**:
   - Konfigurasi `build.gradle.kts` dengan Retrofit, Kotlinx Serialization, Room KSP, dan Material 3 icons.
   - Pastikan permission `android.permission.INTERNET` di `AndroidManifest.xml` serta `GEMINI_API_KEY` di `.env.example`.
2. **Database & Persistence Layer**:
   - Bangun Room Database (`TransactionEntity`, `TransactionDao`, `AppDatabase`).
   - Implementasikan `PreferencesRepository` untuk batas anggaran harian.
3. **AI Chat Service**:
   - Setup Retrofit client untuk endpoint `v1beta/models/gemini-3.5-flash:generateContent`.
   - Parser fallback cerdas berbasis regex jika terjadi kendala jaringan (sehingga pengguna tetap bisa mencatat offline via chat!).
4. **UI Components & Screens**:
   - `HomeScreen`: Ringkasan anggaran harian (meteran sisa budget), kartu status keuangan, tombol pencatatan manual.
   - `AiChatScreen`: Tampilan pesan obrolan interaktif dengan quick-prompts dan kartu auto-save.
   - `SummaryScreen`: Grafik distribusi pengeluaran per kategori dan riwayat lengkap dengan filter.
   - `SettingsDialog`: Pengaturan nominal batas harian.
5. **Quality Verification**:
   - Eksekusi `compile_applet` untuk memastikan build bersih tanpa error.
