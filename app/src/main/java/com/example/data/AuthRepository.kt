package com.example.data

import com.example.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<User?>
    val rememberMeFlow: Flow<Pair<Boolean, String>>
    suspend fun login(email: String, password: String, rememberMe: Boolean): Result<User>
    suspend fun signUp(name: String, email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun updateName(newName: String)
    suspend fun deleteAccount()
}

class DefaultAuthRepository(
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override val currentUserFlow: Flow<User?> = dataStoreManager.userFlow
    override val rememberMeFlow: Flow<Pair<Boolean, String>> = dataStoreManager.rememberMeFlow

    override suspend fun login(email: String, password: String, rememberMe: Boolean): Result<User> {
        delay(1200)
        if (!email.contains("@") || password.length < 6) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }

        val name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        val user = User(
            id = "user_${email.hashCode()}",
            name = name.ifBlank { "Sen Scholar" },
            email = email,
            joinedDate = "October 2026",
            avatarSticker = "📓",
            isLoggedIn = true
        )
        dataStoreManager.saveUser(user, rememberMe, email)
        return Result.success(user)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        delay(1200)
        if (!email.contains("@") || password.length < 6 || name.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide valid details"))
        }

        val user = User(
            id = "user_${email.hashCode()}",
            name = name.trim(),
            email = email.trim(),
            joinedDate = "October 2026",
            avatarSticker = "📓",
            isLoggedIn = true
        )
        dataStoreManager.saveUser(user, rememberMe = true, emailToRemember = email)
        return Result.success(user)
    }

    override suspend fun logout() {
        dataStoreManager.saveUser(null, rememberMe = false, emailToRemember = "")
    }

    override suspend fun updateName(newName: String) {
        dataStoreManager.updateUserName(newName)
    }

    override suspend fun deleteAccount() {
        dataStoreManager.wipeUserData()
    }
}
