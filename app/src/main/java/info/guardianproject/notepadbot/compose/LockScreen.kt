package info.guardianproject.notepadbot.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.database.NoteCipherAmbient

@Preview
@Composable
fun PreviewLockScreen() {
    // LockScreen()
}

@Composable
fun PasswordTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    OutlinedTextField(
        modifier = modifier,
        onValueChange = onValueChange,
        value = value,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardActions = keyboardActions,
        label = {
            Text("Password")
        },
        trailingIcon = {
            Icon(Icons.Filled.VpnKey, "Password")
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun LockScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Box(
        modifier
            .fillMaxSize()
            .padding(16.dp), Alignment.BottomEnd
    ) {
        TextButton(onClick = { navController.navigate(R.id.settingsFragment) }) {
            Text("Settings")
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth()) {
            var password by remember { mutableStateOf(TextFieldValue("")) }
            var canEdit by remember { mutableStateOf(true) }
            val context = LocalContext.current

            Spacer(Modifier.size(16.dp))
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                keyboardActions = KeyboardActions(
                    onDone = {
                        canEdit = false
                        NoteCipherAmbient.unlock(context, password.text)
                    }
                )
            )
            Spacer(Modifier.size(16.dp))
            Row {
                OutlinedButton(onClick = {
                    canEdit = false
                }, enabled = canEdit) {
                    Text("Biometric")
                }
                Spacer(Modifier.size(16.dp))
                Button(onClick = { canEdit = false }, Modifier.fillMaxWidth(), enabled = canEdit) {
                    Text("Unlock")
                    NoteCipherAmbient.unlock(context, password.text)
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