package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import kotlinx.coroutines.launch

@Composable
fun SenCraftLogo(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    showGlow: Boolean = true,
    floatUpOnAppear: Boolean = false,
    contentDescription: String = "SenCraft Logo"
) {
    val alphaAnim = remember { Animatable(if (floatUpOnAppear) 0f else 1f) }
    val offsetYAnim = remember { Animatable(if (floatUpOnAppear) 16f else 0f) }

    if (floatUpOnAppear) {
        LaunchedEffect(Unit) {
            launch {
                alphaAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
            launch {
                offsetYAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .alpha(alphaAnim.value)
            .offset(y = offsetYAnim.value.dp)
            .size(size)
            .drawBehind {
                if (showGlow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SenBrightEmerald.copy(alpha = 0.45f),
                                SenMint.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(this.size.width / 2f, this.size.height / 2f),
                            radius = this.size.width * 0.72f
                        ),
                        radius = this.size.width * 0.72f
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sencraft_logo),
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    }
}
