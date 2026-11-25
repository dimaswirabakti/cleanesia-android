package com.cleanesia.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

/**
 * Ambil satu posisi terkini. Mengembalikan null bila izin belum diberikan,
 * tidak ada provider yang aktif, atau posisi tidak didapat.
 * Fungsi ini tidak meminta izin; izin diminta di layar Landing.
 */
@SuppressLint("MissingPermission")
suspend fun currentLocationOrNull(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val manager = context.getSystemService(LocationManager::class.java) ?: return null
    val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .firstOrNull { manager.isProviderEnabled(it) } ?: return null

    return suspendCancellableCoroutine { cont ->
        val signal = CancellationSignal()
        cont.invokeOnCancellation { signal.cancel() }
        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            signal,
            ContextCompat.getMainExecutor(context),
        ) { location -> if (cont.isActive) cont.resume(location) }
    }
}
