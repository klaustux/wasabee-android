package rocks.wasabee.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import rocks.wasabee.android.data.GoogleAuth
import rocks.wasabee.android.data.GoogleAuthResult
import rocks.wasabee.android.data.WasabeeServer
import rocks.wasabee.android.ui.LoginScreen
import rocks.wasabee.android.ui.MainViewModel
import rocks.wasabee.android.ui.OperationsScreen
import rocks.wasabee.android.ui.UiState

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private var pendingServer: WasabeeServer = WasabeeServer.EU

    private val consentLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        runCatching { GoogleAuth.tokenFromConsent(this, result.data) }
            .onSuccess { viewModel.loginWithGoogle(pendingServer, it) }
            .onFailure { viewModel.showError(it.message) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            viewModel = viewModel(factory = viewModelFactory {
                initializer { MainViewModel((application as WasabeeApp).repository) }
            })
            val state by viewModel.state.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val dark = isSystemInDarkTheme()
            val colors = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                dark -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colors) {
                Surface(Modifier.fillMaxSize()) {
                    when (val current = state) {
                        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        is UiState.LoggedOut -> LoginScreen(
                            error = current.error,
                            onGoogleLogin = ::startGoogleLogin,
                            onOneTimeTokenLogin = viewModel::loginWithOneTimeToken,
                        )
                        is UiState.LoggedIn -> OperationsScreen(
                            state = current,
                            onRefresh = viewModel::refresh,
                            onLogout = viewModel::logout,
                        )
                    }
                }
            }
        }
    }

    private fun startGoogleLogin(server: WasabeeServer) {
        pendingServer = server
        lifecycleScope.launch {
            runCatching { GoogleAuth.authorize(this@MainActivity) }
                .onSuccess { result ->
                    when (result) {
                        is GoogleAuthResult.Token -> viewModel.loginWithGoogle(server, result.accessToken)
                        is GoogleAuthResult.NeedsConsent -> consentLauncher.launch(
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        )
                    }
                }
                .onFailure { viewModel.showError(it.message) }
        }
    }
}
