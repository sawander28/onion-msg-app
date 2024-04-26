@file:OptIn(ExperimentalMaterial3Api::class)

package info.guardianproject.torproxy.arti

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import info.guardianproject.artiservice.ArtiService
import info.guardianproject.torproxy.arti.state.IArtiServicesPreferences
import info.guardianproject.torproxy.arti.state.ArtiServicesPreferences
import info.guardianproject.torproxy.arti.ui.theme.ArtiandroidTheme
import info.guardianproject.torproxy.arti.BuildConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = ArtiServicesPreferences.with(this)

        setContent {
            ArtiandroidTheme {
                MainScreen(prefs)
            }
        }

        requestNotificationPermission()

        if (ArtiServicesPreferences.with(this).isEnabled()) {
            Log.i("###", "App.onCreate(): tor service enabled")
            ArtiService.startService(this)
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean ->
        // Nothing to do here, if we got permission we just use it later, if it was denied
        // permissions simply won't show up. No need to bother users about it any further.
        // The only notification in Save at the moment of writing this comment is showing up
        // during media uploads.
    }

    private fun requestNotificationPermission() {
        Log.i("###", "MainActivity.requestNotificationPermission()")

        if (Build.VERSION.SDK_INT >= 33) {
            Log.i(
                "###",
                "MainActivity.requestNotificationPermission() android version requires permission"
            )
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(
                    "###",
                    "MainActivity.requestNotificationPermission() permission not granted, requesting from user"
                )
                //  ask for the permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun MainScreen(prefs: IArtiServicesPreferences) {
    var enabled: Boolean by remember { mutableStateOf(prefs.isEnabled()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("TorServices (Arti)")
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "${BuildConfig.VERSION_NAME} with Arti ...",
                )
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "SOCKS5: localhost:9150",
                )
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "DNS: localhost:9151",
                )
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "running",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(vertical = 8.dp)
                    )
                    Switch(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = enabled,
                        enabled = false,
                        onCheckedChange = {
                            prefs.setEnabled(it)
                            enabled = it
                            if (it) {
                                ArtiService.startService(context)
                            } else {
                                ArtiService.stopService(context)
                            }
                        },
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val prefs = object : IArtiServicesPreferences {
        override fun isEnabled() = true
        override fun setEnabled(enabled: Boolean) = throw NotImplementedError()
    }
    ArtiandroidTheme {
        MainScreen(prefs)
    }
}