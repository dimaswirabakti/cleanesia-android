package com.cleanesia.feature.report.photo

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.io.File

class ReportPhotoViewModel(
    private val app: Application,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    /**
     * Siapkan file tujuan foto di filesDir/foto. Path-nya disimpan ke SavedStateHandle
     * sebelum kamera dibuka, supaya hasil kamera tetap bisa diproses walau proses
     * aplikasi dimatikan selagi aplikasi kamera terbuka.
     */
    fun newPhotoFile(): File {
        val dir = File(app.filesDir, PHOTO_DIR).apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.jpg").apply { createNewFile() }
        savedState[KEY_PENDING_PATH] = file.absolutePath
        return file
    }

    /**
     * Terima hasil kamera. Mengembalikan path foto bila berhasil.
     * Bila batal atau file kosong, file dihapus dan hasilnya null.
     */
    fun onPhotoResult(success: Boolean): String? {
        val path = savedState.remove<String>(KEY_PENDING_PATH) ?: return null
        val file = File(path)
        if (success && file.length() > 0) return path
        file.delete()
        return null
    }

    companion object {
        private const val PHOTO_DIR = "foto"
        private const val KEY_PENDING_PATH = "pendingPhotoPath"

        val Factory = viewModelFactory {
            initializer {
                ReportPhotoViewModel(this[APPLICATION_KEY]!!, createSavedStateHandle())
            }
        }
    }
}
