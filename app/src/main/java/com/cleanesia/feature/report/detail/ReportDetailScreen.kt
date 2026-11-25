package com.cleanesia.feature.report.detail

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cleanesia.R
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType
import com.cleanesia.ui.theme.Background
import com.cleanesia.ui.theme.Brand100
import com.cleanesia.ui.theme.Brand50
import com.cleanesia.ui.theme.Brand500
import com.cleanesia.ui.theme.Brand700
import com.cleanesia.ui.theme.Ink600
import com.cleanesia.ui.theme.Ink900
import com.cleanesia.ui.theme.Line
import java.util.Locale

private val CardShape = RoundedCornerShape(6.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ReportDetailViewModel,
    onBack: () -> Unit,
    onSent: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    LaunchedEffect(state.sent) { if (state.sent) onSent() }
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        // Setelah ditolak, sistem tidak lagi menampilkan dialog bila rationale bernilai false.
        val permanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        viewModel.onPermissionResult(granted, permanentlyDenied)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporkan Sampah") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    scrolledContainerColor = Background,
                ),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            StepIndicator()
            Spacer(Modifier.height(24.dp))
            PhotoPreview(state)
            Spacer(Modifier.height(24.dp))
            LocationCard(
                location = state.location,
                onRetry = { reason ->
                    when (reason) {
                        LocationUi.Failed.Reason.NO_PERMISSION -> requestLocation.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                        LocationUi.Failed.Reason.PERMISSION_BLOCKED -> activity?.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", activity.packageName, null),
                            )
                        )
                        LocationUi.Failed.Reason.UNAVAILABLE -> viewModel.fetchLocation()
                    }
                },
            )
            Spacer(Modifier.height(16.dp))
            WasteTypeCard(selected = state.types, onToggle = viewModel::toggleType)
            Spacer(Modifier.height(16.dp))
            SeverityCard(selected = state.severity, onSelect = viewModel::selectSeverity)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::send,
                enabled = state.canSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(containerColor = Brand700, contentColor = Color.White),
            ) {
                if (state.sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Kirim Laporan", style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
                }
            }
        }
    }
}

/** Langkah 1 Foto, 2 Detail (aktif), 3 Selesai. */
@Composable
private fun StepIndicator() {
    Row(verticalAlignment = Alignment.Top) {
        StepDot(number = 1, label = "Foto", active = false)
        StepLine()
        StepDot(number = 2, label = "Detail", active = true)
        StepLine()
        StepDot(number = 3, label = "Selesai", active = false)
    }
}

@Composable
private fun StepDot(number: Int, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (active) Brand500 else Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.titleMedium,
                color = if (active) Color.White else Ink600,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (active) Brand700 else Ink600,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepLine() {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(top = 16.dp)
            .height(1.dp)
            .background(Line),
    )
}

@Composable
private fun PhotoPreview(state: ReportDetailUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(CardShape)
            .background(Brand50),
    ) {
        state.photo?.let {
            Image(
                bitmap = it,
                contentDescription = "Foto laporan",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Line), CardShape)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun LocationCard(location: LocationUi, onRetry: (LocationUi.Failed.Reason) -> Unit) {
    Card {
        Text("Lokasi", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        when (location) {
            LocationUi.Searching -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Brand500, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Mencari lokasi…", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            }
            is LocationUi.Found -> Text(
                text = String.format(Locale.US, "%.5f, %.5f", location.lat, location.lng),
                style = MaterialTheme.typography.bodyMedium,
            )
            is LocationUi.Failed -> {
                Text(
                    text = when (location.reason) {
                        LocationUi.Failed.Reason.NO_PERMISSION -> "Izin lokasi belum diberikan."
                        LocationUi.Failed.Reason.PERMISSION_BLOCKED ->
                            "Izin lokasi ditolak. Izinkan lewat pengaturan aplikasi."
                        LocationUi.Failed.Reason.UNAVAILABLE ->
                            "Lokasi belum didapat. Pastikan lokasi perangkat aktif."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink600,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onRetry(location.reason) },
                    shape = CardShape,
                    border = BorderStroke(1.dp, Brand700),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand700),
                ) {
                    Text(
                        if (location.reason == LocationUi.Failed.Reason.PERMISSION_BLOCKED) "Buka Pengaturan"
                        else "Coba lagi"
                    )
                }
            }
        }
    }
}

@Composable
private fun WasteTypeCard(selected: Set<WasteType>, onToggle: (WasteType) -> Unit) {
    Card {
        Text("Jenis Sampah", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        WasteType.entries.sortedBy { it.index }.forEach { type ->
            val active = type in selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .clickable { onToggle(type) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (active) Brand500 else Brand50, CircleShape)
                        .border(1.dp, if (active) Brand500 else Brand100, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(if (active) R.drawable.ic_check else R.drawable.ic_add),
                        contentDescription = null,
                        tint = if (active) Color.White else Brand700,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(type.label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeverityCard(selected: Severity?, onSelect: (Severity) -> Unit) {
    Card {
        Text("Tingkat Keparahan", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Severity.entries.forEach { severity ->
                val isSelected = severity == selected
                Text(
                    text = severity.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Ink900,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Brand500 else Brand50)
                        .clickable { onSelect(severity) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
