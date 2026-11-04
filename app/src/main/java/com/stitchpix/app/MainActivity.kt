package com.stitchpix.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import com.stitchpix.app.ui.AppTheme
import com.stitchpix.app.ui.EditorScreen
import com.stitchpix.app.ui.SelectionScreen
import com.stitchpix.app.ui.SplashScreen

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme(vm.themeMode) { Surface(Modifier.fillMaxSize()) { App(vm) } } }
    }
}

@Composable
private fun App(vm: MainViewModel) {
    val ctx = LocalContext.current
    val readPerm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
    else Manifest.permission.READ_EXTERNAL_STORAGE
    val perms = remember {
        buildList {
            add(readPerm)
            if (Build.VERSION.SDK_INT <= 28) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }.toTypedArray()
    }
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, readPerm) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        granted = it[readPerm] == true
    }
    var minTime by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(700); minTime = true }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(perms) }
    LaunchedEffect(granted, vm.tab) { if (granted) vm.load() }
    BackHandler(enabled = vm.mode != null) { if (!vm.saving) vm.closeEditor() }

    when {
        !minTime || (granted && !vm.loaded) -> SplashScreen()
        !granted -> Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Stitch needs access to your photos to let you pick images.",
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Button(onClick = { launcher.launch(perms) }, modifier = Modifier.padding(top = 16.dp)) { Text("Grant access") }
            TextButton(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
            }) { Text("Open app settings") }
        }
        vm.mode == null -> SelectionScreen(vm)
        else -> EditorScreen(vm)
    }
}
