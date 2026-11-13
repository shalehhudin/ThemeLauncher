package com.example.themelauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val PanelBg = Color(0xFF14161B)
private val ChipOn = Color(0xFF3B82F6)
private val ChipOff = Color(0xFF2A2E37)
private val Muted = Color(0xFFB0B6C2)

@Composable
fun SettingsPanel(
    s: Settings,
    onChange: (Settings) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
) {
    val fonts = remember { fontOptions.indices.map { fontFamilyOf(it) } }

    // Layar utama tetap terlihat di atas panel sebagai pratinjau langsung
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.56f),
            color = PanelBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp
        ) {
            Column(Modifier.navigationBarsPadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pengaturan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ChipOn)
                            .clickable { onClose() }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) { Text("Selesai", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Section("Tema")
                    ChipRow {
                        themes.forEachIndexed { i, th ->
                            Chip(s.themeIdx == i, { onChange(s.copy(themeIdx = i)) }) {
                                val c1 = if (th.wallpaper) Color.Gray else th.top
                                val c2 = if (th.wallpaper) Color.DarkGray else th.bottom
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .background(Brush.linearGradient(listOf(c1, c2)), CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(th.name, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }

                    Section("Font")
                    ChipRow {
                        fontOptions.forEachIndexed { i, f ->
                            Chip(s.fontIdx == i, { onChange(s.copy(fontIdx = i)) }) {
                                Text(f.first, color = Color.White, fontSize = 14.sp, fontFamily = fonts[i])
                            }
                        }
                    }
                    SliderRow("Ukuran tanggal", s.dateSize, 40..110, "sp") { onChange(s.copy(dateSize = it)) }
                    SliderRow("Ukuran teks nama aplikasi", s.labelSize, 10..22, "sp") { onChange(s.copy(labelSize = it)) }

                    Section("Ikon")
                    SliderRow("Ukuran ikon", s.iconSize, 28..80, "dp") { onChange(s.copy(iconSize = it)) }
                    Label("Bentuk ikon")
                    ChipRow {
                        listOf("Asli", "Bulat", "Kotak halus").forEachIndexed { i, n ->
                            Chip(s.iconShape == i, { onChange(s.copy(iconShape = i)) }) {
                                Text(n, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                    SwitchRow("Tampilkan nama aplikasi", s.showLabels) { onChange(s.copy(showLabels = it)) }

                    Section("Tata letak")
                    ChipRow {
                        listOf("Daftar", "Grid").forEachIndexed { i, n ->
                            Chip(s.layout == i, { onChange(s.copy(layout = i)) }) {
                                Text(n, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                    if (s.layout == 1) {
                        SliderRow("Jumlah kolom", s.gridCols, 3..6, "") { onChange(s.copy(gridCols = it)) }
                    }
                    SliderRow("Tinggi gelombang", s.waveHeight, 0..120, "dp") { onChange(s.copy(waveHeight = it)) }
                    SliderRow("Posisi pembagi warna", s.split, 30..70, "%") { onChange(s.copy(split = it)) }

                    Section("Informasi di layar utama")
                    SwitchRow("Tanggal & hari", s.showDate) { onChange(s.copy(showDate = it)) }
                    SwitchRow("Jam", s.showClock) { onChange(s.copy(showClock = it)) }
                    SwitchRow("Format 24 jam", s.use24h) { onChange(s.copy(use24h = it)) }
                    SwitchRow("Sapaan", s.showGreeting) { onChange(s.copy(showGreeting = it)) }
                    SwitchRow("Baterai", s.showBattery) { onChange(s.copy(showBattery = it)) }
                    OutlinedTextField(
                        value = s.userName,
                        onValueChange = { onChange(s.copy(userName = it)) },
                        label = { Text("Nama untuk sapaan") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChipOff)
                            .clickable { onReset() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) { Text("Kembalikan ke pengaturan awal", color = Color.White, fontSize = 13.sp) }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tips: tekan lama layar utama untuk membuka pengaturan ini, geser ke atas atau ketuk tombol bulat untuk daftar aplikasi.",
                        color = Muted, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(16.dp))
    Text(title, color = Color(0xFF7DB5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun Label(text: String) {
    Text(text, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun SliderRow(label: String, value: Int, range: IntRange, suffix: String, onChange: (Int) -> Unit) {
    Column(Modifier.padding(top = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, fontSize = 14.sp)
            Text("$value$suffix", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun Chip(selected: Boolean, onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ChipOn else ChipOff)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
