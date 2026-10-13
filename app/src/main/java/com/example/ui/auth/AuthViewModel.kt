package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object SuccessCheckmark : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(true)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private val _signUpName = MutableStateFlow("")
    val signUpName: StateFlow<String> = _signUpName.asStateFlow()

    private val _signUpEmail = MutableStateFlow("")
    val signUpEmail: StateFlow<String> = _signUpEmail.asStateFlow()

    private val _signUpPassword = MutableStateFlow("")
    val signUpPassword: StateFlow<String> = _signUpPassword.asStateFlow()

    private val _signUpConfirmPassword = MutableStateFlow("")
    val signUpConfirmPassword: StateFlow<String> = _signUpConfirmPassword.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _shakeEvent = MutableSharedFlow<String>() // emits field key to shake
    val shakeEvent: SharedFlow<String> = _shakeEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.rememberMeFlow.collect { (remember, savedEmail) ->
                _rememberMe.value = remember
                if (remember && savedEmail.isNotBlank()) {
                    _email.value = savedEmail
                }
            }
        }
    }

    fun onEmailChange(newVal: String) { _email.value = newVal }
    fun onPasswordChange(newVal: String) { _password.value = newVal }
    fun onRememberMeChange(newVal: Boolean) { _rememberMe.value = newVal }

    fun onSignUpNameChange(newVal: String) { _signUpName.value = newVal }
    fun onSignUpEmailChange(newVal: String) { _signUpEmail.value = newVal }
    fun onSignUpPasswordChange(newVal: String) { _signUpPassword.value = newVal }
    fun onSignUpConfirmPasswordChange(newVal: String) { _signUpConfirmPassword.value = newVal }

    fun login(onSuccess: () -> Unit) {
        val em = _email.value.trim()
        val pw = _password.value

        if (!em.contains("@")) {
            viewModelScope.launch {
                _shakeEvent.emit("email")
                _snackbarEvent.emit("Please enter a valid email address with @")
            }
            return
        }

        if (pw.length < 6) {
            viewModelScope.launch {
                _shakeEvent.emit("password")
                _snackbarEvent.emit("Password must be at least 6 characters")
            }
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            delay(1000)
            val res = authRepository.login(em, pw, _rememberMe.value)
            if (res.isSuccess) {
                _loginState.value = AuthUiState.SuccessCheckmark
                delay(600)
                onSuccess()
                delay(400)
                _loginState.value = AuthUiState.Idle
            } else {
                _loginState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Login failed")
                _shakeEvent.emit("password")
                _snackbarEvent.emit(res.exceptionOrNull()?.message ?: "Invalid email or password")
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        val nm = _signUpName.value.trim()
        val em = _signUpEmail.value.trim()
        val pw = _signUpPassword.value
        val cpw = _signUpConfirmPassword.value

        if (nm.isBlank()) {
            viewModelScope.launch {
                _shakeEvent.emit("signup_name")
                _snackbarEvent.emit("Please enter your name")
            }
            return
        }
        if (!em.contains("@")) {
            viewModelScope.launch {
                _shakeEvent.emit("signup_email")
                _snackbarEvent.emit("Please enter a valid email with @")
            }
            return
        }
        if (pw.length < 6) {
            viewModelScope.launch {
                _shakeEvent.emit("signup_password")
                _snackbarEvent.emit("Password must be at least 6 characters")
            }
            return
        }
        if (pw != cpw) {
            viewModelScope.launch {
                _shakeEvent.emit("signup_confirm")
                _snackbarEvent.emit("Passwords do not match")
            }
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            delay(1000)
            val res = authRepository.signUp(nm, em, pw)
            if (res.isSuccess) {
                _loginState.value = AuthUiState.SuccessCheckmark
                delay(600)
                onSuccess()
                delay(400)
                _loginState.value = AuthUiState.Idle
            } else {
                _loginState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            authRepository.updateName(newName)
            _snackbarEvent.emit("Name updated successfully")
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount()
            _email.value = ""
            _password.value = ""
            _rememberMe.value = false
            _signUpName.value = ""
            _signUpEmail.value = ""
            _signUpPassword.value = ""
            _signUpConfirmPassword.value = ""
            _loginState.value = AuthUiState.Idle
            onDeleted()
        }
    }
}
