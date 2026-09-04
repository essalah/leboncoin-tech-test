package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adevinta.spark.SparkTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.navigation.AppNavHost

/**
 * Fix: the app used to have two launcher Activities (this one and the now-removed
 * `DetailsActivity`, which was wrongly declared with its own `MAIN`/`LAUNCHER` intent-filter —
 * see AndroidManifest.xml history). Consolidating to a single Activity hosting a
 * Navigation-Compose graph removes that bug at the root instead of patching the manifest: there
 * is now only one entry point, and it's structurally impossible to navigate to a screen without
 * passing the data it needs (the album id is a real nav argument, not an omitted Intent extra).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SparkTheme {
                AppNavHost()
            }
        }
    }
}
