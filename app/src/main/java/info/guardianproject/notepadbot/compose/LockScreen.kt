package info.guardianproject.notepadbot.compose

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.insets.imePadding
import info.guardianproject.notepadbot.R

@Preview
@Composable
fun PreviewLockScreen() {
    LockScreen()
}

@Composable
fun LockScreen(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .padding(16.dp), Alignment.BottomEnd
    ) {
        TextButton(onClick = { /*TODO*/ }) {
            Text("Settings")
        }
    }

    val imageSize = 300.dp
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth()) {
            val text = remember { mutableStateOf(TextFieldValue("")) }

            Spacer(Modifier.size(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text.value,
                onValueChange = {
                    text.value = it
                },
                label = {
                    Text("password")
                },
                trailingIcon = {
                    Icon(Icons.Filled.VpnKey, "password")
                },
            )
            Spacer(Modifier.size(16.dp))
            Row {
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Text("Biometric")
                }
                Spacer(Modifier.size(16.dp))
                Button(onClick = { /*TODO*/ }, Modifier.fillMaxWidth()) {
                    Text("Unlock")
                }
            }
        }
    }

    /*ConstraintLayout(modifier.fillMaxSize()) {

        val (settingsButton, unlockButton, biometricButton, passwordField, logo) = createRefs()



        Image(painterResource(R.drawable.ic_launcher_foreground), "Logo",
            Modifier.constrainAs(logo) {
                top.linkTo(parent.top)
                bottom.linkTo(passwordField.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })

    }*/
}