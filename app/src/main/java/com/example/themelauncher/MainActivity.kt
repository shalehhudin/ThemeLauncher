package com.example.themelauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Screen { Home, Drawer, Settings }

class MainActivity : ComponentActivity() {

    private var screen by mutableStateOf(Screen.Home)
    private var tick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Tombol Home ditekan lagi -> kembali ke layar utama
        addOnNewIntentListener { screen = Screen.Home }
        val prefs = getSharedPreferences("launcher", MODE_PRIVATE)

        setContent {
            var settings by remember { mutableStateOf(SettingsStore.load(prefs)) }
            var favs by remember { mutableStateOf(SettingsStore.loadFavs(prefs)) }
            var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

            LaunchedEffect(tick) { apps = withContext(Dispatchers.Default) { loadApps() } }
            BackHandler(true) { screen = Screen.Home }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Box(Modifier.fillMaxSize()) {
                    HomeScreen(
                        s = settings,
                        apps = apps,
                        favs = favs,
                        onLaunch = { launchApp(it) },
                        onOpenDrawer = { screen = Screen.Drawer },
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    if (screen == Screen.Drawer) {
                        DrawerScreen(
                            s = settings,
                            apps = apps,
                            favs = favs,
                            onLaunch = { launchApp(it) },
                            onToggleFav = { pkg ->
                                val cur = SettingsStore.loadFavs(prefs)
                                val next = if (pkg in cur) cur - pkg else cur + pkg
                                SettingsStore.saveFavs(prefs, next)
                                favs = next
                                Toast.makeText(
                                    this@MainActivity,
                                    if (pkg in cur) "Dihapus dari layar utama" else "Ditambahkan ke layar utama",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onSettings = { screen = Screen.Settings },
                        )
                    }
                    if (screen == Screen.Settings) {
                        SettingsPanel(
                            s = settings,
                            onChange = {
                                settings = it
                                SettingsStore.save(prefs, it)
                            },
                            onReset = {
                                settings = Settings()
                                SettingsStore.save(prefs, settings)
                            },
                            onClose = { screen = Screen.Home },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tick++ // muat ulang daftar aplikasi (kalau ada yang baru diinstal/dihapus)
    }

    private fun launchApp(pkg: String) {
        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
        screen = Screen.Home
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map {
                AppItem(
                    label = it.loadLabel(pm).toString(),
                    pkg = it.activityInfo.packageName,
                    icon = it.loadIcon(pm).toBitmap(128, 128).asImageBitmap()
                )
            }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }
}
