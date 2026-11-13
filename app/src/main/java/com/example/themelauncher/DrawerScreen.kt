package com.example.themelauncher

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerScreen(
    s: Settings,
    apps: List<AppItem>,
    favs: List<String>,
    onLaunch: (String) -> Unit,
    onToggleFav: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val t = themes[s.themeIdx.coerceIn(0, themes.lastIndex)]
    val font = remember(s.fontIdx) { fontFamilyOf(s.fontIdx) }
    var q by remember { mutableStateOf("") }
    val list = remember(apps, q) { apps.filter { it.label.contains(q, ignoreCase = true) } }
    val bg = if (t.wallpaper) Color(0xEE000000) else t.top.copy(alpha = 0.97f)

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = q,
                onValueChange = { q = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontFamily = font),
                cursorBrush = SolidColor(t.accent),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (q.isEmpty()) {
                            BasicText(
                                "Cari aplikasi...",
                                style = TextStyle(color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp, fontFamily = font)
                            )
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                BasicText("\u2699", style = TextStyle(color = Color.White, fontSize = 22.sp))
            }
        }
        Spacer(Modifier.height(6.dp))
        BasicText(
            "Tekan lama ikon untuk menambah/menghapus dari layar utama",
            style = TextStyle(color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontFamily = font)
        )
        Spacer(Modifier.height(6.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f)) {
            items(list, key = { it.pkg }) { app ->
                Box(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                        .tapOrLong(app.pkg, { onLaunch(app.pkg) }, { onToggleFav(app.pkg) })
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIcon(app, s.iconSize.coerceAtLeast(44), s.iconShape)
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            app.label, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                color = Color.White, fontSize = 11.sp,
                                textAlign = TextAlign.Center, fontFamily = font
                            )
                        )
                    }
                    if (app.pkg in favs) {
                        BasicText(
                            "\u2605",
                            modifier = Modifier.align(Alignment.TopEnd).padding(end = 6.dp),
                            style = TextStyle(color = Color(0xFFFFD54F), fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}
