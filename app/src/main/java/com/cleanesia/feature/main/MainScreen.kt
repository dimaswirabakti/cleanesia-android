package com.cleanesia.feature.main

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.cleanesia.R
import com.cleanesia.feature.map.PetaScreen
import com.cleanesia.feature.report.photo.ReportPhotoActivity
import com.cleanesia.ui.components.PlaceholderScreen
import com.cleanesia.ui.theme.Brand100
import com.cleanesia.ui.theme.Ink900
import com.cleanesia.ui.theme.Surface

private enum class Tab(val label: String, @param:DrawableRes val icon: Int) {
    BERANDA("Beranda", R.drawable.ic_nav_home),
    PETA("Peta", R.drawable.ic_nav_map),
    LAPORAN("Laporan", R.drawable.ic_nav_camera),
    AKTIVITAS("Aktivitas", R.drawable.ic_nav_activity),
    PROFIL("Profil", R.drawable.ic_nav_profile),
}

@Composable
fun MainScreen() {
    var selected by rememberSaveable { mutableStateOf(Tab.BERANDA) }
    val context = LocalContext.current

    Scaffold(
        // Inset atas ditangani tiap tab (TopAppBar), jadi Scaffold luar hanya memberi ruang bottom bar.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                Tab.entries.forEach { tab ->
                    val isSelected = tab == selected
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            // Tab Laporan membuka alur lapor di Activity terpisah; tab aktif tidak berubah.
                            if (tab == Tab.LAPORAN) {
                                context.startActivity(Intent(context, ReportPhotoActivity::class.java))
                            } else {
                                selected = tab
                            }
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink900,
                            selectedTextColor = Ink900,
                            indicatorColor = Brand100,
                            unselectedIconColor = Ink900,
                            unselectedTextColor = Ink900,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (selected) {
                Tab.PETA -> PetaScreen()
                else -> PlaceholderScreen(title = selected.label)
            }
        }
    }
}
