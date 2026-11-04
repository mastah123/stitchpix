package com.stitchpix.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media as M

object MediaRepository {
    @Suppress("DEPRECATION")
    fun query(ctx: Context, screenshotsOnly: Boolean): List<MediaItem> {
        val projection = arrayOf(M._ID, M.DISPLAY_NAME, M.DATE_ADDED, M.WIDTH, M.HEIGHT, M.ORIENTATION)
        val selection = if (screenshotsOnly) "${M.DATA} LIKE ?" else null
        val args = if (screenshotsOnly) arrayOf("%/screenshot%/%") else null
        val out = ArrayList<MediaItem>()
        ctx.contentResolver.query(M.EXTERNAL_CONTENT_URI, projection, selection, args, "${M.DATE_ADDED} DESC")
            ?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val w = c.getInt(3)
                    val h = c.getInt(4)
                    val rot = c.getInt(5)
                    val ratio = if (w > 0 && h > 0) {
                        (if (rot == 90 || rot == 270) h.toFloat() / w else w.toFloat() / h).coerceIn(0.2f, 5f)
                    } else 1f
                    out += MediaItem(id, ContentUris.withAppendedId(M.EXTERNAL_CONTENT_URI, id),
                        c.getString(1) ?: "", c.getLong(2), ratio)
                }
            }
        return out
    }
}
