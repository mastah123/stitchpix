package com.stitchpix.app.data

import android.net.Uri

enum class Tab { SCREENSHOTS, ALL }
enum class ThemeMode(val label: String) { SYSTEM("System default"), LIGHT("Light"), DARK("Dark") }
enum class Mode { HORIZONTAL, VERTICAL }
enum class SortOrder(val label: String) {
    NEWEST("Added recently"), OLDEST("Added earliest"), NAME_ASC("Name A–Z"), NAME_DESC("Name Z–A")
}

/** ratio = width / height after EXIF orientation. */
data class MediaItem(val id: Long, val uri: Uri, val name: String, val dateAdded: Long, val ratio: Float)

/** Crop range as fractions (0..1) along the stitch axis of the image. */
data class Crop(val start: Float = 0f, val end: Float = 1f)
