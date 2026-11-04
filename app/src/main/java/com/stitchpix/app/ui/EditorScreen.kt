package com.stitchpix.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stitchpix.app.MainViewModel
import com.stitchpix.app.data.Crop
import com.stitchpix.app.data.MediaItem
import com.stitchpix.app.data.Mode

// Preview zoom: smaller values show more of the stitch at once.
private const val VERTICAL_ZOOM = 0.65f
private val HORIZONTAL_HEIGHT = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(vm: MainViewModel) {
    val horizontal = vm.mode == Mode.HORIZONTAL
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(vm.message) { vm.message?.let { snack.showSnackbar(it); vm.message = null } }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(if (horizontal) "Horizontal stitch" else "Vertical stitch") },
                navigationIcon = {
                    IconButton(onClick = vm::closeEditor) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = { IconButton(onClick = vm::openSave) { Icon(Icons.Filled.Save, "Save") } }
            )
        }
    ) { pad ->
        val items = vm.selected.toList()
        if (horizontal) {
            LazyRow(
                Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(items, key = { it.id }) { item ->
                    Column {
                        CropImage(item, vm.crops[item.id] ?: Crop(), true, HORIZONTAL_HEIGHT, vm.croppingId == item.id) { vm.setCrop(item.id, it) }
                        CutControls(vm, item, Modifier.align(Alignment.CenterHorizontally), row = true)
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                items(items, key = { it.id }) { item ->
                    Row(Modifier.fillMaxWidth()) {
                        BoxWithConstraints(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CropImage(item, vm.crops[item.id] ?: Crop(), false, maxWidth * VERTICAL_ZOOM, vm.croppingId == item.id) { vm.setCrop(item.id, it) }
                        }
                        CutControls(vm, item, Modifier.width(56.dp), row = false)
                    }
                }
            }
        }
        if (vm.showSave) SaveDialog(vm)
    }
}

/** Cut icon (right of the image in vertical mode, below it in horizontal mode) + check to finish. */
@Composable
private fun CutControls(vm: MainViewModel, item: MediaItem, modifier: Modifier, row: Boolean) {
    val active = vm.croppingId == item.id
    val content: @Composable () -> Unit = {
        if (!active) {
            IconButton(onClick = { vm.toggleCropping(item.id) }) { Icon(Icons.Filled.ContentCut, "Crop image ${item.name}") }
        } else {
            FilledIconButton(onClick = { vm.toggleCropping(item.id) }) { Icon(Icons.Filled.ContentCut, "Cropping") }
            FilledIconButton(onClick = { vm.toggleCropping(item.id) }) { Icon(Icons.Filled.Check, "Done cropping") }
        }
    }
    if (row) Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    else Column(modifier.padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
}
