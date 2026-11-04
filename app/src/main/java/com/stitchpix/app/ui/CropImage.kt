package com.stitchpix.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stitchpix.app.data.Crop
import com.stitchpix.app.data.MediaItem

/**
 * Shows one image along the stitch axis. [thickness] is the fixed cross-axis size
 * (width in vertical mode, height in horizontal mode). While [cropping], the full image is
 * shown with two draggable handles (top/bottom or left/right only).
 */
@Composable
fun CropImage(
    item: MediaItem, crop: Crop, horizontal: Boolean, thickness: Dp, cropping: Boolean, onCrop: (Crop) -> Unit
) {
    val full = if (horizontal) thickness * item.ratio else thickness / item.ratio
    val visible = if (cropping) full else full * (crop.end - crop.start)
    val shift = if (cropping) 0.dp else full * crop.start
    val boxW = if (horizontal) visible else thickness
    val boxH = if (horizontal) thickness else visible
    val cur by rememberUpdatedState(crop)
    // Keep the two handles at least 48dp apart so they never overlap.
    val minSpan = (48.dp / full).coerceIn(0.02f, 0.5f)
    val primary = MaterialTheme.colorScheme.primary

    Box(Modifier.size(boxW, boxH).clipToBounds().then(if (cropping) Modifier.border(2.dp, primary) else Modifier)) {
        AsyncImage(
            model = item.uri, contentDescription = item.name, contentScale = ContentScale.FillBounds,
            modifier = Modifier
                // Top-start aligned, unbounded: the image keeps its full size and is only clipped by the box.
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .size(if (horizontal) full else thickness, if (horizontal) thickness else full)
                .offset(x = if (horizontal) -shift else 0.dp, y = if (horizontal) 0.dp else -shift)
        )
        if (cropping) {
            val dim = Color.Black.copy(alpha = 0.55f)
            val startLen = full * crop.start
            val endLen = full * (1f - crop.end)
            if (horizontal) {
                Box(Modifier.width(startLen).fillMaxHeight().background(dim))
                Box(Modifier.align(Alignment.CenterEnd).width(endLen).fillMaxHeight().background(dim))
            } else {
                Box(Modifier.height(startLen).fillMaxWidth().background(dim))
                Box(Modifier.align(Alignment.BottomStart).height(endLen).fillMaxWidth().background(dim))
            }
            Handle(horizontal, full, thickness, full * crop.start) {
                onCrop(cur.copy(start = (cur.start + it).coerceIn(0f, cur.end - minSpan)))
            }
            Handle(horizontal, full, thickness, full * crop.end) {
                onCrop(cur.copy(end = (cur.end + it).coerceIn(cur.start + minSpan, 1f)))
            }
        }
    }
}

@Composable
private fun Handle(horizontal: Boolean, length: Dp, thickness: Dp, pos: Dp, onDelta: (Float) -> Unit) {
    val density = LocalDensity.current
    val callback by rememberUpdatedState(onDelta)
    val state = rememberDraggableState { px -> callback(with(density) { px.toDp() } / length) }
    val primary = MaterialTheme.colorScheme.primary
    val modifier = if (horizontal)
        Modifier.offset(x = pos - 20.dp).size(40.dp, thickness).draggable(state, Orientation.Horizontal)
    else
        Modifier.offset(y = pos - 20.dp).size(thickness, 40.dp).draggable(state, Orientation.Vertical)
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(if (horizontal) Modifier.width(3.dp).fillMaxHeight().background(primary)
            else Modifier.height(3.dp).fillMaxWidth().background(primary))
        Box(Modifier.size(if (horizontal) 14.dp else 44.dp, if (horizontal) 44.dp else 14.dp)
            .clip(RoundedCornerShape(7.dp)).background(primary))
    }
}
