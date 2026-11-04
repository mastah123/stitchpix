package com.stitchpix.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.stitchpix.app.MainViewModel
import com.stitchpix.app.data.MediaItem
import com.stitchpix.app.data.Mode
import com.stitchpix.app.data.SortOrder
import com.stitchpix.app.data.ThemeMode
import com.stitchpix.app.data.Tab

@Composable
fun SelectionScreen(vm: MainViewModel) {
    val items = remember(vm.images, vm.sort) { vm.sortedImages() }
    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Header(vm)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (items.isEmpty()) {
                Text("No images found", Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(3),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 104.dp),
                verticalItemSpacing = 10.dp,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }, contentType = { "image" }) { item ->
                    Tile(item, vm.selected.indexOfFirst { it.id == item.id }) { vm.toggle(item) }
                }
            }
            BottomBar(vm, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun Header(vm: MainViewModel) {
    var menu by remember { mutableStateOf(false) }
    var themeMenu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        HeaderTab("Screenshots", vm.tab == Tab.SCREENSHOTS) { vm.tab = Tab.SCREENSHOTS }
        HeaderTab("All photos", vm.tab == Tab.ALL) { vm.tab = Tab.ALL }
        Spacer(Modifier.weight(1f))
        Box {
            IconButton(onClick = { themeMenu = true }) { Icon(Icons.Filled.Brightness6, contentDescription = "Theme") }
            DropdownMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                ThemeMode.values().forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.label) },
                        onClick = { vm.chooseTheme(t); themeMenu = false },
                        trailingIcon = { if (vm.themeMode == t) Icon(Icons.Filled.Check, contentDescription = "Selected") }
                    )
                }
            }
        }
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                SortOrder.values().forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.label) },
                        onClick = { vm.sort = s; menu = false },
                        trailingIcon = { if (vm.sort == s) Icon(Icons.Filled.Check, contentDescription = "Selected") }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Tile(item: MediaItem, index: Int, onClick: () -> Unit) {
    val selected = index >= 0
    val shape = RoundedCornerShape(12.dp)
    val ctx = LocalContext.current
    val request = remember(item.uri) {
        ImageRequest.Builder(ctx).data(item.uri).size(Size(400, 400)).allowRgb565(true).build()
    }
    Box(
        Modifier.fillMaxWidth().aspectRatio(item.ratio)
            .clip(shape)
            .border(
                if (selected) 4.dp else 1.5.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(request, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (selected) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
            Box(
                Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) { Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun BottomBar(vm: MainViewModel, modifier: Modifier) {
    AnimatedVisibility(
        visible = vm.selected.size >= 2,
        modifier = modifier,
        enter = slideInVertically { it }, exit = slideOutVertically { it }
    ) {
        Surface(Modifier.padding(16.dp), shape = RoundedCornerShape(28.dp), shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { vm.openEditor(Mode.HORIZONTAL) }) {
                    Icon(Icons.Filled.ViewColumn, contentDescription = "Horizontal stitch")
                }
                Button(onClick = { vm.openEditor(Mode.VERTICAL) }) {
                    Icon(Icons.Filled.ViewAgenda, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Vertical stitch")
                }
            }
        }
    }
}
