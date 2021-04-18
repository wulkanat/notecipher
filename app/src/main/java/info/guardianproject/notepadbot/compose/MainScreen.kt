package info.guardianproject.notepadbot.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.*
import java.util.concurrent.locks.Lock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.fragment.fragment
import dagger.hilt.android.AndroidEntryPoint
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.CacheWordSettings
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import info.guardianproject.notepadbot.fragments.SettingsFragment

class MainScreenActivity : AppCompatActivity() {
    @ExperimentalAnimationApi
    @ExperimentalAnimatedInsets
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            NoteCipherTheme {
                // https://google.github.io/accompanist/insets/
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    MainScreen()
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "lockScreen") {
        composable("lockScreen") {
            LockScreen(Modifier.navigationBarsWithImePadding())
        }
        composable("mainScreen") {
            Text("This is the main screen")
        }
        composable("writeNote") {
            WriteNote()
        }
        composable("settingsScreen") {

        }
    }
}

@ExperimentalAnimationApi
@ExperimentalAnimatedInsets
@Preview
@Composable
fun PreviewMain() {
    MainScreen()
}