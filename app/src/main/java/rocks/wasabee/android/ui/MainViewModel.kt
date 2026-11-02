package rocks.wasabee.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import rocks.wasabee.android.data.Me
import rocks.wasabee.android.data.WasabeeRepository
import rocks.wasabee.android.data.WasabeeServer

sealed interface UiState {
    data object Loading : UiState
    data class LoggedOut(val error: String? = null) : UiState
    data class LoggedIn(val me: Me, val refreshing: Boolean = false, val error: String? = null) : UiState
}

class MainViewModel(private val repository: WasabeeRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = try {
                repository.restoreSession()?.let { UiState.LoggedIn(it) } ?: UiState.LoggedOut()
            } catch (e: Exception) {
                if (e.isUnauthorized()) repository.logout()
                UiState.LoggedOut(e.message)
            }
        }
    }

    fun loginWithGoogle(server: WasabeeServer, accessToken: String) =
        login { repository.loginWithGoogle(server, accessToken) }

    fun loginWithOneTimeToken(server: WasabeeServer, token: String) =
        login { repository.loginWithOneTimeToken(server, token) }

    fun showError(message: String?) {
        _state.value = UiState.LoggedOut(message)
    }

    fun refresh() {
        val current = _state.value as? UiState.LoggedIn ?: return
        _state.value = current.copy(refreshing = true, error = null)
        viewModelScope.launch {
            _state.value = try {
                UiState.LoggedIn(repository.me())
            } catch (e: Exception) {
                if (e.isUnauthorized()) {
                    repository.logout()
                    UiState.LoggedOut("Session expired, log in again")
                } else {
                    current.copy(refreshing = false, error = e.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = UiState.LoggedOut()
        }
    }

    private fun login(block: suspend () -> Me) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = try {
                UiState.LoggedIn(block())
            } catch (e: Exception) {
                UiState.LoggedOut(e.toUserMessage())
            }
        }
    }

    private fun Exception.isUnauthorized() = this is HttpException && code() == 401

    private fun Exception.toUserMessage(): String = when {
        this is HttpException && code() == 403 -> "Access denied by Wasabee server"
        this is HttpException -> "Server error ${code()}"
        else -> message ?: javaClass.simpleName
    }
}
