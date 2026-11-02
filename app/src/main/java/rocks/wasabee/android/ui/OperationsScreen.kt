package rocks.wasabee.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import rocks.wasabee.android.data.Me

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationsScreen(
    state: UiState.LoggedIn,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    val me: Me = state.me
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(me.name.ifBlank { "Operations" }) },
                actions = { TextButton(onClick = onLogout) { Text("Log out") } },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                state.error?.let { error ->
                    item { Text(error, color = MaterialTheme.colorScheme.error) }
                }
                if (me.ops.isEmpty()) {
                    item { ListItem(headlineContent = { Text("No operations") }) }
                }
                items(me.ops, key = { it.id }) { op ->
                    ListItem(
                        headlineContent = { Text(op.name) },
                        supportingContent = {
                            Text(listOfNotNull(if (op.isOwner) "Owner" else null, op.modified.ifBlank { null }).joinToString(" · "))
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
