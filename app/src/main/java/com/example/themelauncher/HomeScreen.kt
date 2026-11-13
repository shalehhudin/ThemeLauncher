package com.example.themelauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BatteryInfo(val level: Int, val charging: Boolean)

fun readBattery(ctx: Context): BatteryInfo {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL
    return BatteryInfo(if (level >= 0 && scale > 0) level * 100 / scale else 0, charging)
}

fun Modifier.tapOrLong(key: Any, onTap: () -> Unit, onLong: () -> Unit = {}): Modifier =
    pointerInput(key) { detectTapGestures(onTap = { onTap() }, onLongPress = { onLong() }) }

@Composable
fun AppIcon(app: AppItem, size: Int, shape: Int) {
    val sh = when (shape) {
        1 -> CircleShape
        2 -> RoundedCornerShape((size * 0.28f).dp)
        else -> RectangleShape
    }
    Image(
        bitmap = app.icon,
        contentDescription = app.label,
        modifier = Modifier.size(size.dp).clip(sh)
    )
}

@Composable
fun HomeScreen(
    s: Settings,
    apps: List<AppItem>,
    favs: List<String>,
    onLaunch: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val t = themes[s.themeIdx.coerceIn(0, themes.lastIndex)]
    val font = remember(s.fontIdx) { fontFamilyOf(s.fontIdx) }
    val ctx = LocalContext.current

    val now by produceState(System.currentTimeMillis()) {
        while (true) { value = System.currentTimeMillis(); delay(10_000) }
    }
    val battery by produceState(BatteryInfo(100, false)) {
        while (true) { value = readBattery(ctx); delay(20_000) }
    }

    val loc = Locale.getDefault()
    val d = Date(now)
    val day = SimpleDateFormat("d", loc).format(d)
    val month = SimpleDateFormat("MMMM", loc).format(d).replaceFirstChar { it.uppercase() }
    val weekday = SimpleDateFormat("EEEE", loc).format(d).replaceFirstChar { it.uppercase() }
    val timeStr = SimpleDateFormat(if (s.use24h) "HH:mm" else "hh:mm a", loc).format(d)
    val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
    val sapa = when {
        hour < 11 -> "Selamat pagi"
        hour < 15 -> "Selamat siang"
        hour < 18 -> "Selamat sore"
        else -> "Selamat malam"
    }
    val greet = if (s.userName.isBlank()) sapa else "$sapa, ${s.userName.trim()}"

    val favApps = remember(apps, favs) {
        val byPkg = apps.associateBy { it.pkg }
        val chosen = favs.mapNotNull { byPkg[it] }
        if (chosen.isEmpty()) apps.take(7) else chosen
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onOpenSettings() }) }
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (total < -150f) onOpenDrawer() },
                    onVerticalDrag = { _, dy -> total += dy }
                )
            }
    ) {
        WaveBackground(t, s)

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            if (s.showDate) {
                Row(verticalAlignment = Alignment.Bottom) {
                    BasicText(
                        day,
                        style = TextStyle(
                            color = t.headText, fontSize = s.dateSize.sp,
                            fontWeight = FontWeight.Bold, fontFamily = font
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicText(
                        month,
                        style = TextStyle(
                            color = t.headText, fontSize = (s.dateSize * 0.62f).sp,
                            fontWeight = FontWeight.Bold, fontFamily = font
                        )
                    )
                }
                BasicText(
                    weekday,
                    modifier = Modifier.offset(
                        x = (s.dateSize * 0.9f).dp,
                        y = (-(s.dateSize * 0.2f)).dp
                    ),
                    style = TextStyle(
                        color = t.accent, fontSize = (s.dateSize * 0.6f).sp,
                        fontFamily = FontFamily.Cursive
                    )
                )
            }

            if (s.showClock || s.showGreeting) {
                Spacer(Modifier.height(8.dp))
                if (s.showClock) {
                    BasicText(
                        timeStr,
                        style = TextStyle(
                            color = t.headText, fontSize = 24.sp,
                            fontWeight = FontWeight.Bold, fontFamily = font
                        )
                    )
                }
                if (s.showGreeting) {
                    BasicText(
                        greet,
                        style = TextStyle(
                            color = t.headText.copy(alpha = 0.85f), fontSize = 14.sp, fontFamily = font
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth(0.7f)
                        .height(1.dp)
                        .background(t.headText.copy(alpha = 0.3f))
                )
            }

            if (s.showBattery) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(36.dp)) {
                        val sw = 5.dp.toPx()
                        val arcSize = Size(size.width - sw, size.height - sw)
                        val tl = Offset(sw / 2f, sw / 2f)
                        drawArc(
                            color = t.headText.copy(alpha = 0.25f), startAngle = 0f, sweepAngle = 360f,
                            useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw)
                        )
                        drawArc(
                            color = t.accent, startAngle = -90f,
                            sweepAngle = 360f * battery.level / 100f,
                            useCenter = false, topLeft = tl, size = arcSize,
                            style = Stroke(sw, cap = StrokeCap.Round)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        BasicText(
                            "${battery.level}%",
                            style = TextStyle(
                                color = t.headText, fontSize = 22.sp,
                                fontWeight = FontWeight.Bold, fontFamily = font
                            )
                        )
                        BasicText(
                            if (battery.charging) "Sedang mengisi daya" else "Baterai",
                            style = TextStyle(
                                color = t.headText.copy(alpha = 0.75f), fontSize = 11.sp, fontFamily = font
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (s.layout == 0) {
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(favApps, key = { it.pkg }) { app ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .tapOrLong(app.pkg, { onLaunch(app.pkg) }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(app, s.iconSize, s.iconShape)
                            if (s.showLabels) {
                                Spacer(Modifier.width(14.dp))
                                BasicText(
                                    app.label, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        color = t.listText, fontSize = s.labelSize.sp,
                                        fontWeight = FontWeight.Medium, fontFamily = font
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(s.gridCols.coerceIn(3, 6)),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(favApps, key = { it.pkg }) { app ->
                        Column(
                            Modifier
                                .padding(vertical = 8.dp, horizontal = 2.dp)
                                .tapOrLong(app.pkg, { onLaunch(app.pkg) }),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppIcon(app, s.iconSize, s.iconShape)
                            if (s.showLabels) {
                                Spacer(Modifier.height(4.dp))
                                BasicText(
                                    app.label, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        color = t.listText, fontSize = (s.labelSize - 3).coerceAtLeast(9).sp,
                                        textAlign = TextAlign.Center, fontFamily = font
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tombol bulat -> daftar semua aplikasi
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .systemBarsPadding()
                .padding(24.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(t.fab, CircleShape)
                .clickable { onOpenDrawer() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(22.dp)) {
                val step = size.width / 3f
                for (r in 0..2) for (c in 0..2) {
                    drawCircle(
                        Color.White, radius = step * 0.22f,
                        center = Offset(step * (c + 0.5f), step * (r + 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveBackground(t: LTheme, s: Settings) {
    if (t.wallpaper) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.55f))
                    )
                )
        )
    } else {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val base = h * s.split.coerceIn(25, 75) / 100f
            val amp = s.waveHeight.dp.toPx()
            drawRect(t.bottom)
            val p = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, base - amp)
                cubicTo(w * 0.7f, base + amp * 1.8f, w * 0.35f, base - amp * 1.8f, 0f, base + amp)
                close()
            }
            drawPath(p, t.top)
        }
    }
}
