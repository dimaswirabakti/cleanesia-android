package com.cleanesia.feature.report.done

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleanesia.R
import com.cleanesia.ui.theme.Background
import com.cleanesia.ui.theme.Brand700
import com.cleanesia.ui.theme.Ink600
import com.cleanesia.ui.theme.Success

@Composable
fun ReportDoneScreen(onBackToHome: () -> Unit) {
    // Back sistem sama dengan tombol: tidak ada gunanya kembali ke layar Foto setelah laporan tersimpan.
    BackHandler(onBack = onBackToHome)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Success, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Laporan Tersimpan!", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Laporan kamu sudah tersimpan di perangkat dan tampil di peta.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink600,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand700, contentColor = Color.White),
        ) {
            Text("Kembali ke Beranda", style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
        }
    }
}
