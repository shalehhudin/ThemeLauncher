package com.example.themelauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppItem(val label: String, val pkg: String, val icon: ImageBitmap)

data class LTheme(val name: String, val bg: List<Color>, val accent: Color, val text: Color)

// Daftar tema. Tambah/ubah warna di sini. Tema dengan bg kosong = pakai wallpaper sistem.
val themes = listOf(
    LTheme("Midnight", listOf(Color(0xFF0F172A), Color(0xFF312E81)), Color(0xFF818CF8), Color.White),
    LTheme("Sunset", listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)), Color(0xFF7C2D12), Color.White),
    LTheme("Forest", listOf(Color(0xFF064E3B), Color(0xFF065F46)), Color(0xFF6EE7B7), Color.White),
    LTheme("Ocean", listOf(Color(0xFF0369A1), Color(0xFF22D3EE)), Color(0xFFE0F2FE), Color.White),
    LTheme("Sakura", listOf(Color(0xFFFBCFE8), Color(0xFFF9A8D4)), Color(0xFF9D174D), Color(0xFF4A044E)),
    LTheme("Wallpaper", emptyList(), Color(0xFFFFFFFF), Color.White),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)

        setContent {
            var themeIndex by remember { mutableIntStateOf(prefs.getInt("idx", 0).coerceIn(0, themes.lastIndex)) }
            var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

            LaunchedEffect(Unit) { apps = withContext(Dispatchers.Default) { loadApps() } }

            LauncherScreen(
                apps = apps,
                themeIndex = themeIndex,
                onTheme = {
                    themeIndex = it
                    prefs.edit().putInt("idx", it).apply()
                },
                onLaunch = { pkg -> packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) } }
            )
        }
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map {
                AppItem(
                    label = it.loadLabel(pm).toString(),
                    pkg = it.activityInfo.packageName,
                    icon = it.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
                )
            }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }
}

@Composable
fun LauncherScreen(
    apps: List<AppItem>,
    themeIndex: Int,
    onTheme: (Int) -> Unit,
    onLaunch: (String) -> Unit,
) {
    val t = themes[themeIndex]
    var query by remember { mutableStateOf("") }
    val shown = remember(apps, query) { apps.filter { it.label.contains(query, ignoreCase = true) } }

    val time by produceState("") {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            value = fmt.format(Date())
            delay(1000)
        }
    }
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }

    val bgMod = if (t.bg.isEmpty()) Modifier else Modifier.background(Brush.verticalGradient(t.bg))

    Column(
        Modifier
            .fillMaxSize()
            .then(bgMod)
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        BasicText(time, style = TextStyle(color = t.text, fontSize = 56.sp))
        BasicText(date, style = TextStyle(color = t.text.copy(alpha = 0.8f), fontSize = 16.sp))
        Spacer(Modifier.height(16.dp))

        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = t.text, fontSize = 16.sp),
            cursorBrush = SolidColor(t.accent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (query.isEmpty()) {
                        BasicText("Cari aplikasi...", style = TextStyle(color = t.text.copy(alpha = 0.6f), fontSize = 16.sp))
                    }
                    inner()
                }
            }
        )
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f)
        ) {
            items(shown, key = { it.pkg }) { app ->
                Column(
                    Modifier
                        .clickable { onLaunch(app.pkg) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(52.dp))
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        app.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(color = t.text, fontSize = 11.sp, textAlign = TextAlign.Center)
                    )
                }
            }
        }

        // Pemilih tema
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            themes.forEachIndexed { i, th ->
                val brush = if (th.bg.isEmpty())
                    Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Red))
                else Brush.linearGradient(th.bg)
                Box(
                    Modifier
                        .size(if (i == themeIndex) 40.dp else 30.dp)
                        .background(brush, CircleShape)
                        .clickable { onTheme(i) }
                )
            }
        }
    }
}
