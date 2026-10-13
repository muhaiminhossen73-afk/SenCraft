package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedCheckmarkGlow
import com.example.ui.components.SenCraftLogo
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenFieldFill
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import com.example.util.AppLinks
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val name by viewModel.signUpName.collectAsState()
    val email by viewModel.signUpEmail.collectAsState()
    val password by viewModel.signUpPassword.collectAsState()
    val confirmPassword by viewModel.signUpConfirmPassword.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        viewModel.shakeEvent.collectLatest {
            for (i in 0..2) {
                shakeAnim.animateTo(10f, tween(50))
                shakeAnim.animateTo(-10f, tween(50))
            }
            shakeAnim.animateTo(0f, tween(50))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // SenCraft Logo (88dp) centered in upper area with soft emerald glow and float-up
            SenCraftLogo(
                size = 88.dp,
                showGlow = true,
                floatUpOnAppear = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    val title = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)) {
                            append("Create ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("Account")
                        }
                    }
                    Text(text = title, color = SenTextWhite, fontSize = 34.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Start capturing thoughts in luxury style", color = SenTextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Name Field
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 60)) + slideInVertically(tween(400, delayMillis = 60)) { 40 }
            ) {
                var focused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onSignUpNameChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text("Your full name", color = SenTextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Name",
                            tint = if (focused) SenBrightEmerald else SenTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SenTextWhite,
                        unfocusedTextColor = SenTextWhite,
                        focusedContainerColor = SenFieldFill,
                        unfocusedContainerColor = SenFieldFill,
                        focusedBorderColor = SenBrightEmerald,
                        unfocusedBorderColor = SenBorderStrong
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Email Field
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 120)) + slideInVertically(tween(400, delayMillis = 120)) { 40 }
            ) {
                var focused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.onSignUpEmailChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text("Email address", color = SenTextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email",
                            tint = if (focused) SenBrightEmerald else SenTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SenTextWhite,
                        unfocusedTextColor = SenTextWhite,
                        focusedContainerColor = SenFieldFill,
                        unfocusedContainerColor = SenFieldFill,
                        focusedBorderColor = SenBrightEmerald,
                        unfocusedBorderColor = SenBorderStrong
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 180)) + slideInVertically(tween(400, delayMillis = 180)) { 40 }
            ) {
                var focused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onSignUpPasswordChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text("Password (min 6 characters)", color = SenTextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Password",
                            tint = if (focused) SenBrightEmerald else SenTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = SenTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SenTextWhite,
                        unfocusedTextColor = SenTextWhite,
                        focusedContainerColor = SenFieldFill,
                        unfocusedContainerColor = SenFieldFill,
                        focusedBorderColor = SenBrightEmerald,
                        unfocusedBorderColor = SenBorderStrong
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Password Field
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(tween(400, delayMillis = 240)) { 40 }
            ) {
                var focused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.onSignUpConfirmPasswordChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text("Confirm password", color = SenTextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Confirm Password",
                            tint = if (focused) SenBrightEmerald else SenTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = SenTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SenTextWhite,
                        unfocusedTextColor = SenTextWhite,
                        focusedContainerColor = SenFieldFill,
                        unfocusedContainerColor = SenFieldFill,
                        focusedBorderColor = SenBrightEmerald,
                        unfocusedBorderColor = SenBorderStrong
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.signUp(onSignUpSuccess)
                    })
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Create Account Button with morphing animation
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300)) { 40 }
            ) {
                val isButtonLoading = loginState is AuthUiState.Loading
                val isButtonSuccess = loginState is AuthUiState.SuccessCheckmark

                val buttonWidth by animateDpAsState(
                    targetValue = if (isButtonLoading || isButtonSuccess) 54.dp else 360.dp,
                    animationSpec = spring(stiffness = 400f),
                    label = "btn_morph_width_signup"
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier
                            .width(buttonWidth)
                            .height(54.dp)
                            .clip(CircleShape)
                            .clickable(enabled = loginState is AuthUiState.Idle) {
                                focusManager.clearFocus()
                                viewModel.signUp(onSignUpSuccess)
                            },
                        shape = CircleShape,
                        color = if (isButtonSuccess) SenBrightEmerald else SenAccentEmerald,
                        border = BorderStroke(1.dp, SenBorderStrong)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isButtonLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SenTextWhite,
                                    strokeWidth = 2.5.dp
                                )
                            } else if (isButtonSuccess) {
                                AnimatedCheckmarkGlow(sizeDp = 48)
                            } else {
                                Text(
                                    text = "Create Account",
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Requirement 5: Privacy Policy consent text under Create Account button
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400, delayMillis = 320))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By creating an account you agree to our ",
                        color = SenTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Privacy Policy.",
                        color = SenMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            AppLinks.openUrl(context, AppLinks.PRIVACY_POLICY_URL)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Link: Already have an account? Log In
            val footerString = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(SpanStyle(color = SenBrightEmerald, fontWeight = FontWeight.Bold)) {
                    append("Log In")
                }
            }
            Text(
                text = footerString,
                color = SenTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onNavigateToLogin)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}
