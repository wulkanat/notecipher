package info.guardianproject.notepadbot.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberImeNestedScrollConnection

@ExperimentalAnimatedInsets
@Composable
fun WriteNote() {
    Column {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(connection = rememberImeNestedScrollConnection())) {
            TextField(modifier = Modifier
                .fillMaxWidth()
                .height(700.dp), value = "Title", onValueChange = {})

        }
        TextField(modifier = Modifier
            .fillMaxWidth()
            .navigationBarsWithImePadding(),value = "Body", onValueChange = {})
    }
}

@ExperimentalAnimatedInsets
@Preview
@Composable
fun PreviewWriteNote() {
    WriteNote()
}