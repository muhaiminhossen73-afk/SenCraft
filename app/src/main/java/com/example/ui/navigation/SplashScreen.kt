package com.example.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        delay(400) // gentle pause before continue
        onSplashFinished()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // SenCraft Logo (112dp) with soft emerald glow
            com.example.ui.components.SenCraftLogo(
                size = 112.dp,
                showGlow = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SenCraft",
                color = SenTextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Dark Luxury Sanctuary",
                color = SenTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
