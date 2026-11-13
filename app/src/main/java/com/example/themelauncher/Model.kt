package com.example.themelauncher

import android.content.SharedPreferences
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily

data class AppItem(val label: String, val pkg: String, val icon: ImageBitmap)

data class LTheme(
    val name: String,
    val top: Color,        // warna area atas
    val bottom: Color,     // warna area bawah
    val headText: Color,   // teks di area atas
    val listText: Color,   // teks daftar aplikasi (area bawah)
    val accent: Color,     // warna hari (script) & lingkaran baterai
    val fab: Color,        // warna tombol bulat
    val wallpaper: Boolean = false,
)

// Tambah / ubah tema di sini
val themes = listOf(
    LTheme("Biru Merah", Color(0xFF0B5394), Color(0xFFD9432F), Color.White, Color.White, Color(0xFFE53935), Color(0xFF0B5394)),
    LTheme("Midnight", Color(0xFF0F172A), Color(0xFF5B21B6), Color.White, Color.White, Color(0xFFA78BFA), Color(0xFF0F172A)),
    LTheme("Forest", Color(0xFF064E3B), Color(0xFF65A30D), Color.White, Color.White, Color(0xFFFDE68A), Color(0xFF064E3B)),
    LTheme("Sunset", Color(0xFF9A3412), Color(0xFFF59E0B), Color.White, Color(0xFF431407), Color(0xFFFDE68A), Color(0xFF9A3412)),
    LTheme("Mono", Color(0xFF111827), Color(0xFFE5E7EB), Color.White, Color(0xFF111827), Color(0xFFD1D5DB), Color(0xFF111827)),
    LTheme("Sakura", Color(0xFFBE185D), Color(0xFFFBCFE8), Color.White, Color(0xFF500724), Color(0xFFFDE68A), Color(0xFFBE185D)),
    LTheme("Wallpaper", Color.Transparent, Color.Transparent, Color.White, Color.White, Color.White, Color(0x66000000), wallpaper = true),
)

// Daftar font bawaan Android (nama, nama sistem). Tidak perlu file tambahan.
val fontOptions = listOf(
    "Standar" to "sans-serif",
    "Condensed" to "sans-serif-condensed",
    "Light" to "sans-serif-light",
    "Medium" to "sans-serif-medium",
    "Black" to "sans-serif-black",
    "Serif" to "serif",
    "Mono" to "monospace",
    "Casual" to "casual",
    "Cursive" to "cursive",
)

fun fontFamilyOf(i: Int): FontFamily =
    FontFamily(Typeface.create(fontOptions[i.coerceIn(0, fontOptions.lastIndex)].second, Typeface.NORMAL))

data class Settings(
    val themeIdx: Int = 0,
    val fontIdx: Int = 1,
    val iconSize: Int = 40,
    val labelSize: Int = 15,
    val dateSize: Int = 64,
    val iconShape: Int = 1,       // 0 asli, 1 bulat, 2 kotak halus
    val showLabels: Boolean = true,
    val layout: Int = 0,          // 0 daftar, 1 grid
    val gridCols: Int = 4,
    val waveHeight: Int = 40,
    val split: Int = 48,          // posisi pembagi warna (% tinggi layar)
    val showDate: Boolean = true,
    val showClock: Boolean = true,
    val showGreeting: Boolean = true,
    val showBattery: Boolean = true,
    val use24h: Boolean = true,
    val userName: String = "",
)

object SettingsStore {
    fun load(p: SharedPreferences): Settings {
        val d = Settings()
        return Settings(
            themeIdx = p.getInt("themeIdx", d.themeIdx).coerceIn(0, themes.lastIndex),
            fontIdx = p.getInt("fontIdx", d.fontIdx).coerceIn(0, fontOptions.lastIndex),
            iconSize = p.getInt("iconSize", d.iconSize),
            labelSize = p.getInt("labelSize", d.labelSize),
            dateSize = p.getInt("dateSize", d.dateSize),
            iconShape = p.getInt("iconShape", d.iconShape),
            showLabels = p.getBoolean("showLabels", d.showLabels),
            layout = p.getInt("layout", d.layout),
            gridCols = p.getInt("gridCols", d.gridCols),
            waveHeight = p.getInt("waveHeight", d.waveHeight),
            split = p.getInt("split", d.split),
            showDate = p.getBoolean("showDate", d.showDate),
            showClock = p.getBoolean("showClock", d.showClock),
            showGreeting = p.getBoolean("showGreeting", d.showGreeting),
            showBattery = p.getBoolean("showBattery", d.showBattery),
            use24h = p.getBoolean("use24h", d.use24h),
            userName = p.getString("userName", d.userName) ?: "",
        )
    }

    fun save(p: SharedPreferences, s: Settings) {
        p.edit()
            .putInt("themeIdx", s.themeIdx)
            .putInt("fontIdx", s.fontIdx)
            .putInt("iconSize", s.iconSize)
            .putInt("labelSize", s.labelSize)
            .putInt("dateSize", s.dateSize)
            .putInt("iconShape", s.iconShape)
            .putBoolean("showLabels", s.showLabels)
            .putInt("layout", s.layout)
            .putInt("gridCols", s.gridCols)
            .putInt("waveHeight", s.waveHeight)
            .putInt("split", s.split)
            .putBoolean("showDate", s.showDate)
            .putBoolean("showClock", s.showClock)
            .putBoolean("showGreeting", s.showGreeting)
            .putBoolean("showBattery", s.showBattery)
            .putBoolean("use24h", s.use24h)
            .putString("userName", s.userName)
            .apply()
    }

    fun loadFavs(p: SharedPreferences): List<String> =
        (p.getString("favs", "") ?: "").split(",").filter { it.isNotBlank() }

    fun saveFavs(p: SharedPreferences, favs: List<String>) {
        p.edit().putString("favs", favs.joinToString(",")).apply()
    }
}
