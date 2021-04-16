package info.guardianproject.notepadbot.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.*
import java.util.concurrent.locks.Lock

class MainScreenActivity : AppCompatActivity() {
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            /*BottomAppBar(
                contentPadding = LocalWindowInsets.current.navigationBars.toPaddingValues(),
                // cutoutShape = CircleShape,
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Lock, contentDescription = "Lock")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }*/
        },
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            /*FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }*/
        }
    ) {
        NavHost(navController, startDestination = "lockScreen") {
            composable("lockScreen") {
                LockScreen(Modifier.navigationBarsWithImePadding())
            }
            composable("mainScreen") {
                Text("This is the main screen")
            }
        }
    }
}

@Composable
fun NoteCipherTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Preview
@Composable
fun PreviewMain() {
    MainScreen()
}