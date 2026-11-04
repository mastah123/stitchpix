package com.stitchpix.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stitchpix.app.MainViewModel
import com.stitchpix.app.stitch.Exporter.Format

private fun fmtSize(b: Long) = if (b >= 1_000_000) "%.1f MB".format(b / 1_000_000.0) else "${maxOf(1L, b / 1000)} KB"

@Composable
fun SaveDialog(vm: MainViewModel) {
    var format by remember { mutableStateOf(Format.JPG) }
    var quality by remember { mutableFloatStateOf(90f) }
    val plan = vm.plan
    AlertDialog(
        onDismissRequest = vm::closeSave,
        title = { Text("Save options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (plan == null) {
                    CircularProgressIndicator()
                } else {
                    Text("Image type: ${format.label}")
                    Text("Dimensions: ${plan.width} × ${plan.height} px")
                    Text("Size: ≈ ${fmtSize(com.stitchpix.app.stitch.Exporter.estimateBytes(plan.width, plan.height, format, quality.toInt()))} (estimate)")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Format.values().forEach { f ->
                        FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f.label) }, enabled = !vm.saving)
                    }
                }
                if (format == Format.JPG) {
                    Text("Quality: ${quality.toInt()}%", style = MaterialTheme.typography.labelLarge)
                    Slider(value = quality, onValueChange = { quality = it }, valueRange = 10f..100f, enabled = !vm.saving)
                } else {
                    Text("Quality: lossless (slider applies to JPG only)", style = MaterialTheme.typography.bodySmall)
                }
                if (format == Format.PDF) Text("PDF is saved to Documents/Stitch.", style = MaterialTheme.typography.bodySmall)
                if (vm.saving) CircularProgressIndicator()
            }
        },
        confirmButton = {
            Button(onClick = { vm.save(format, quality.toInt()) }, enabled = plan != null && !vm.saving) { Text("Save to gallery") }
        },
        dismissButton = { TextButton(onClick = vm::closeSave, enabled = !vm.saving) { Text("Cancel") } }
    )
}
