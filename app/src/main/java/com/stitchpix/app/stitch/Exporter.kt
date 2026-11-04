package com.stitchpix.app.stitch

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

object Exporter {
    enum class Format(val label: String, val ext: String, val mime: String) {
        JPG("JPG", "jpg", "image/jpeg"), PNG("PNG", "png", "image/png"), PDF("PDF", "pdf", "application/pdf")
    }

    /** Rough estimate only; real size depends on image content. */
    fun estimateBytes(w: Int, h: Int, f: Format, quality: Int): Long {
        val bytesPerPx = when (f) {
            Format.JPG -> 0.04 + 0.30 * (quality / 100.0).pow(2)
            Format.PNG -> 1.2
            Format.PDF -> 1.5
        }
        return (w.toLong() * h * bytesPerPx).toLong()
    }

    private fun write(bmp: Bitmap, f: Format, q: Int, os: OutputStream) {
        when (f) {
            Format.JPG -> check(bmp.compress(Bitmap.CompressFormat.JPEG, q, os)) { "JPEG encoding failed" }
            Format.PNG -> check(bmp.compress(Bitmap.CompressFormat.PNG, 100, os)) { "PNG encoding failed" }
            Format.PDF -> {
                val doc = PdfDocument()
                try {
                    val page = doc.startPage(PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create())
                    page.canvas.drawBitmap(bmp, 0f, 0f, null)
                    doc.finishPage(page)
                    doc.writeTo(os)
                } finally { doc.close() }
            }
        }
    }

    /** Saves to Pictures/Stitch (images) or Documents/Stitch (PDF). Returns the location. */
    fun save(ctx: Context, bmp: Bitmap, f: Format, q: Int): String {
        val name = "Stitch_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.${f.ext}"
        val folder = if (f == Format.PDF) Environment.DIRECTORY_DOCUMENTS else Environment.DIRECTORY_PICTURES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, f.mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$folder/Stitch")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val collection = if (f == Format.PDF) MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = ctx.contentResolver.insert(collection, values) ?: error("Could not create file")
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { write(bmp, f, q, it) } ?: error("Could not open file")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                ctx.contentResolver.update(uri, values, null, null)
            } catch (e: Throwable) {
                ctx.contentResolver.delete(uri, null, null)
                throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(folder), "Stitch").apply { mkdirs() }
            val file = File(dir, name)
            FileOutputStream(file).use { write(bmp, f, q, it) }
            MediaScannerConnection.scanFile(ctx, arrayOf(file.path), arrayOf(f.mime), null)
        }
        return "$folder/Stitch/$name"
    }
}
