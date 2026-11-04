package com.stitchpix.app.stitch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.stitchpix.app.data.Crop
import com.stitchpix.app.data.MediaItem
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Computes output dimensions (cheap) and renders the final stitched bitmap (heavy). */
object Stitcher {
    private const val MAX_PIXELS = 60_000_000.0

    class Part(val item: MediaItem, val crop: Crop, val rotation: Int, val srcMin: Int, val len: Int)

    class Plan(val horizontal: Boolean, val thickness: Int, val parts: List<Part>) {
        private val length get() = parts.sumOf { it.len }
        val width get() = if (horizontal) length else thickness
        val height get() = if (horizontal) thickness else length
    }

    private class Info(val w: Int, val h: Int, val rotation: Int)

    private fun info(ctx: Context, uri: Uri): Info {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
        if (o.outWidth <= 0 || o.outHeight <= 0) error("Cannot read image")
        val rot = try {
            ctx.contentResolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) { 0 }
        return if (rot == 90 || rot == 270) Info(o.outHeight, o.outWidth, rot) else Info(o.outWidth, o.outHeight, rot)
    }

    fun plan(ctx: Context, items: List<MediaItem>, crops: Map<Long, Crop>, horizontal: Boolean): Plan {
        val infos = items.map { info(ctx, it.uri) }
        val thick = infos.map { if (horizontal) it.h else it.w }
        var t = thick.min().toDouble()
        val lens = items.indices.map { i ->
            val c = crops[items[i].id] ?: Crop()
            val l = if (horizontal) infos[i].w else infos[i].h
            l * (c.end - c.start).toDouble() * t / thick[i]
        }
        val px = t * lens.sum()
        val f = if (px > MAX_PIXELS) sqrt(MAX_PIXELS / px) else 1.0
        t *= f
        val parts = items.indices.map { i ->
            Part(items[i], crops[items[i].id] ?: Crop(), infos[i].rotation,
                minOf(infos[i].w, infos[i].h), (lens[i] * f).roundToInt().coerceAtLeast(1))
        }
        return Plan(horizontal, t.roundToInt().coerceAtLeast(1), parts)
    }

    private fun decode(ctx: Context, p: Part, t: Int): Bitmap {
        var s = 1
        while (p.srcMin / (s * 2) >= t) s *= 2
        val o = BitmapFactory.Options().apply { inSampleSize = s }
        var b = ctx.contentResolver.openInputStream(p.item.uri)?.use { BitmapFactory.decodeStream(it, null, o) }
            ?: error("Cannot decode ${p.item.name}")
        if (p.rotation != 0) {
            val m = Matrix().apply { postRotate(p.rotation.toFloat()) }
            b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
        }
        return b
    }

    fun render(ctx: Context, plan: Plan): Bitmap {
        val out = Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        var pos = 0
        for (p in plan.parts) {
            val bmp = decode(ctx, p, plan.thickness)
            val c = p.crop
            val src = if (plan.horizontal) {
                val x0 = (c.start * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                val w = ((c.end - c.start) * bmp.width).toInt().coerceIn(1, bmp.width - x0)
                Bitmap.createBitmap(bmp, x0, 0, w, bmp.height)
            } else {
                val y0 = (c.start * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                val h = ((c.end - c.start) * bmp.height).toInt().coerceIn(1, bmp.height - y0)
                Bitmap.createBitmap(bmp, 0, y0, bmp.width, h)
            }
            val dw = if (plan.horizontal) p.len else plan.thickness
            val dh = if (plan.horizontal) plan.thickness else p.len
            val scaled = Bitmap.createScaledBitmap(src, dw, dh, true)
            canvas.drawBitmap(scaled, if (plan.horizontal) pos.toFloat() else 0f,
                if (plan.horizontal) 0f else pos.toFloat(), null)
            pos += p.len
            bmp.recycle(); src.recycle(); scaled.recycle()
        }
        return out
    }
}
