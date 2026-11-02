package rocks.wasabee.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rocks.wasabee.android.data.WasabeeServer

@Composable
fun LoginScreen(
    error: String?,
    onGoogleLogin: (WasabeeServer) -> Unit,
    onOneTimeTokenLogin: (WasabeeServer, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var server by rememberSaveable { mutableStateOf(WasabeeServer.EU) }
    var oneTimeToken by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text("Wasabee", style = MaterialTheme.typography.headlineLarge)

        Text("Server", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            WasabeeServer.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = server == entry,
                    onClick = { server = entry },
                    shape = SegmentedButtonDefaults.itemShape(index, WasabeeServer.entries.size),
                ) { Text(entry.label) }
            }
        }

        Button(onClick = { onGoogleLogin(server) }, modifier = Modifier.fillMaxWidth()) {
            Text("Log in with Google")
        }

        HorizontalDivider()

        OutlinedTextField(
            value = oneTimeToken,
            onValueChange = { oneTimeToken = it },
            label = { Text("One-time token (from wasabee.rocks)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { onOneTimeTokenLogin(server, oneTimeToken) },
            enabled = oneTimeToken.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Log in with token") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
