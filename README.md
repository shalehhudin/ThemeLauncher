# Theme Launcher

Launcher Android (Kotlin + Jetpack Compose) bergaya poster:
tanggal besar + hari tulisan tangan, jam, baterai lingkaran, latar dua warna bergelombang,
daftar aplikasi vertikal, dan tombol bulat untuk membuka semua aplikasi.

## Pengaturan (tekan lama layar utama)
- Tema (7 pilihan, termasuk Wallpaper sistem)
- Font (9 pilihan) + ukuran tanggal + ukuran teks nama aplikasi
- Ukuran ikon, bentuk ikon (asli / bulat / kotak halus), tampil/sembunyi nama aplikasi
- Tata letak daftar atau grid (3-6 kolom)
- Tinggi gelombang & posisi pembagi warna
- Tampil/sembunyi tanggal, jam, sapaan, baterai; format 24 jam; nama untuk sapaan
- Perubahan terlihat langsung di layar utama di atas panel

## Cara pakai
- Ketuk tombol bulat (atau geser ke atas): daftar semua aplikasi
- Tekan lama ikon di daftar: tambah/hapus dari layar utama (bintang = favorit)
- Tekan lama layar utama: pengaturan

## Menambah font sendiri
Taruh file .ttf di app/src/main/res/font/, lalu tambahkan ke daftar fontOptions di Model.kt.
