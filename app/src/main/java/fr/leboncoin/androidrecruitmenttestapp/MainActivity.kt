package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.adevinta.spark.SparkTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.navigation.AppNavHost

/**
 * Extends `AppCompatActivity` (not a plain `ComponentActivity`) so it participates in
 * `AppCompatDelegate`'s per-app language backport: on API 24-32, that backport only auto-recreates
 * `AppCompatActivity` subclasses when `PhotoApp` applies a persisted language override.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
