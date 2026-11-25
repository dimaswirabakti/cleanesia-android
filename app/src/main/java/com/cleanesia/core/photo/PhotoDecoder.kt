package com.cleanesia.core.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

/**
 * Baca foto dari path dengan ukuran diperkecil, lalu putar sesuai tag EXIF Orientation.
 * Kamera sering menyimpan piksel apa adanya dan hanya menandai arah putarnya di EXIF,
 * sedangkan BitmapFactory tidak membaca tag itu.
 * Dipanggil di thread latar. Mengembalikan null bila file tidak bisa dibaca.
 */
fun decodePhoto(path: String, maxSidePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSidePx) sample *= 2
    val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return null

    val orientation = runCatching {
        ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = exifMatrix(orientation) ?: return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        .also { if (it !== bitmap) bitmap.recycle() }
}

/** Matriks untuk tiap nilai Orientation EXIF (1..8). Null berarti tidak perlu diubah. */
private fun exifMatrix(orientation: Int): Matrix? = when (orientation) {
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { setScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { setRotate(180f) }
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { setScale(1f, -1f) }
    ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { setRotate(90f); postScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { setRotate(90f) }
    ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply { setRotate(-90f); postScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { setRotate(-90f) }
    else -> null
}
