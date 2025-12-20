# MINDLENS
**Integrasi Jurnal Harian, Deteksi Suasana Hati, dan Klasifikasi Depresi**

Proyek ini diajukan sebagai Tugas Akhir Semester (UAS).
Fakultas Teknik dan Informatika - Program Studi Informatika
Universitas Multimedia Nusantara (2025)

---

## 👥 ANGGOTA KELOMPOK
Berikut adalah daftar anggota kelompok:

1. **Henry Salim** (00000079925)
2. **Livia Junike** (00000076451)
3. **Willbert Budi Lian** (00000082864)
4. **Yehezkiel Natanael** (00000079871)

---

## 🔑 AKSES PENGUJIAN (LOGIN CREDENTIALS)
Untuk memudahkan pengujian tanpa perlu melakukan registrasi akun baru, silakan gunakan akun *dummy* yang telah disiapkan berikut ini:

* **Email:** `john@gmail.com`
* **Password:** `UAS.Map1234`


---

## ⚙️ PETUNJUK INSTALASI & PENGUJIAN

### 1. Persyaratan Sistem
* **Android Studio:** Versi terbaru (Ladybug/Koala recommended).
* **JDK:** Versi 17 atau lebih baru.
* **Perangkat:** Disarankan menggunakan **Physical Device** (HP Asli) untuk performa Kamera & AI yang optimal, namun Emulator tetap didukung.

### 2. Langkah Menjalankan Aplikasi
1.  Buka proyek di Android Studio.
2.  Lakukan **Sync Project with Gradle Files**.
3.  Jalankan (`Run`) aplikasi ke perangkat target.
4.  Pastikan perangkat memiliki koneksi **Internet** (Wajib untuk Supabase Auth & Database).

### 3. Catatan Khusus Fitur (Special Instructions)

#### A. Fitur AI Diagnostic (Depression Classifier)
* **Izin Kamera:** Saat pertama kali membuka menu diagnosis, izinkan akses kamera (*Allow Camera Permission*).
* **Model Machine Learning:** Aplikasi menggunakan model `efficientnet_mobile.ptl` (PyTorch Mobile) yang berjalan secara *On-Device*. Tidak diperlukan koneksi internet untuk proses inferensi gambar, namun diperlukan internet untuk menyimpan hasil ke riwayat (Cloud).
* **Emulator:** Jika menggunakan emulator, pastikan pengaturan kamera depan diatur ke "Webcam0".

#### B. Fitur Mood Tracker & Jurnal Harian
* **Input Data:** Pengguna dapat memilih ikon suasana hati (*Mood*) yang mewakili perasaan mereka saat ini dan menambahkan catatan harian (*Journal Entry*).
* **Penyimpanan:** Data mood dan jurnal disimpan secara *real-time* ke database Supabase (`diary_entries`).
* **Visualisasi:** Riwayat mood dapat dilihat kembali untuk memantau tren emosi pengguna dari waktu ke waktu.

#### C. Fitur Artikel (GNews API)
* Halaman artikel mengambil data secara *real-time* dari GNews API. Jika artikel tidak muncul, periksa koneksi internet Anda.

---

## 🛠️ SPESIFIKASI TEKNIS

* **Bahasa Pemrograman:** Kotlin
* **Arsitektur:** MVVM (Model-View-ViewModel)
* **UI Toolkit:** Jetpack Compose
* **Machine Learning:** PyTorch Mobile (EfficientNet Architecture)
* **Backend & Database:** Supabase (PostgreSQL, Auth, Storage)
* **Networking:** Ktor Client / Supabase SDK
