package com.stitchpix.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stitchpix.app.data.Crop
import com.stitchpix.app.data.MediaItem
import com.stitchpix.app.data.MediaRepository
import com.stitchpix.app.data.Mode
import com.stitchpix.app.data.SortOrder
import com.stitchpix.app.data.ThemeMode
import com.stitchpix.app.data.Tab
import com.stitchpix.app.stitch.Exporter
import com.stitchpix.app.stitch.Stitcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("stitchpix", Context.MODE_PRIVATE)
    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM") ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM)
    )
    var loaded by mutableStateOf(false)
    var images by mutableStateOf<List<MediaItem>>(emptyList())
    var tab by mutableStateOf(Tab.SCREENSHOTS)
    var sort by mutableStateOf(SortOrder.NEWEST)
    var mode by mutableStateOf<Mode?>(null)
    var croppingId by mutableStateOf<Long?>(null)
    var showSave by mutableStateOf(false)
    var plan by mutableStateOf<Stitcher.Plan?>(null)
    var saving by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    val selected = mutableStateListOf<MediaItem>()
    val crops = mutableStateMapOf<Long, Crop>()
    private var loadJob: Job? = null

    fun load() {
        val screenshots = tab == Tab.SCREENSHOTS
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            images = withContext(Dispatchers.IO) { MediaRepository.query(getApplication(), screenshots) }
            loaded = true
        }
    }

    fun chooseTheme(m: ThemeMode) {
        themeMode = m
        prefs.edit().putString("theme", m.name).apply()
    }

    fun sortedImages(): List<MediaItem> = when (sort) {
        SortOrder.NEWEST -> images.sortedByDescending { it.dateAdded }
        SortOrder.OLDEST -> images.sortedBy { it.dateAdded }
        SortOrder.NAME_ASC -> images.sortedBy { it.name.lowercase() }
        SortOrder.NAME_DESC -> images.sortedByDescending { it.name.lowercase() }
    }

    /** Selection order = list order, so numbers shift automatically on deselect. */
    fun toggle(item: MediaItem) {
        val i = selected.indexOfFirst { it.id == item.id }
        if (i >= 0) { selected.removeAt(i); crops.remove(item.id) } else selected.add(item)
    }

    fun openEditor(m: Mode) { mode = m; croppingId = null }
    fun closeEditor() { mode = null; croppingId = null; showSave = false }
    fun toggleCropping(id: Long) { croppingId = if (croppingId == id) null else id }
    fun setCrop(id: Long, c: Crop) { crops[id] = c }

    fun openSave() {
        showSave = true; plan = null; croppingId = null
        val items = selected.toList(); val cr = crops.toMap(); val h = mode == Mode.HORIZONTAL
        viewModelScope.launch {
            try {
                plan = withContext(Dispatchers.Default) { Stitcher.plan(getApplication(), items, cr, h) }
            } catch (e: Throwable) {
                message = "Could not read images: ${e.message}"; showSave = false
            }
        }
    }

    fun closeSave() { if (!saving) showSave = false }

    fun save(format: Exporter.Format, quality: Int) {
        val p = plan ?: return
        saving = true
        viewModelScope.launch {
            message = try {
                val where = withContext(Dispatchers.Default) {
                    val bmp = Stitcher.render(getApplication(), p)
                    try { Exporter.save(getApplication(), bmp, format, quality) } finally { bmp.recycle() }
                }
                "Saved to $where"
            } catch (e: Throwable) {
                "Save failed: ${e.message ?: e.javaClass.simpleName}"
            }
            saving = false; showSave = false
        }
    }
}
