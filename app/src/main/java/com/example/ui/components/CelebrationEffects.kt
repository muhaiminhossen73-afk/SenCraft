package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenTextWhite
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float,
    val symbol: String? = null
)

/**
 * Animated checkmark with pulsing green glow
 */
@Composable
fun AnimatedCheckmarkGlow(
    modifier: Modifier = Modifier,
    sizeDp: Int = 48
) {
    val scaleAnim = remember { Animatable(0f) }
    val glowAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        glowAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(scaleAnim.value)
            .drawBehind {
                val glowRadius = size.width * (0.8f + 0.4f * glowAnim.value)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SenBrightEmerald.copy(alpha = 0.6f * (1f - glowAnim.value * 0.3f)),
                            SenMint.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = glowRadius
                    ),
                    radius = glowRadius
                )
            }
            .background(SenBrightEmerald, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Success",
            tint = Color(0xFF050B08),
            modifier = Modifier.size((sizeDp * 0.6f).dp)
        )
    }
}

/**
 * Mint coin burst upward animation for coin purchases
 */
@Composable
fun CoinBurstOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        List(24) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = 250f + Random.nextFloat() * 450f
            Particle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed * 0.8f,
                vy = -speed - Random.nextFloat() * 200f, // bias upward!
                color = if (it % 2 == 0) SenMint else SenBrightEmerald,
                radius = 6f + Random.nextFloat() * 8f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.6f
        val t = progress.value
        val alpha = (1f - t).coerceIn(0f, 1f)

        particles.forEach { p ->
            val px = cx + p.vx * t
            val py = cy + p.vy * t + (500f * t * t) // gravity
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.radius * (1f - t * 0.3f),
                center = Offset(px, py)
            )
            // Coin shine
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = p.radius * 0.4f,
                center = Offset(px - 2f, py - 2f)
            )
        }
    }
}

/**
 * Hearts and sparkles burst for "Buy Me Something" gift celebration
 */
@Composable
fun HeartSparklesBurstOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        List(28) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = 200f + Random.nextFloat() * 400f
            Particle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 150f,
                color = if (it % 3 == 0) SenMint else if (it % 3 == 1) SenBrightEmerald else Color.White,
                radius = 5f + Random.nextFloat() * 7f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1400, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.5f
        val t = progress.value
        val alpha = (1f - t).coerceIn(0f, 1f)

        particles.forEach { p ->
            val px = cx + p.vx * t
            val py = cy + p.vy * t + (350f * t * t)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.radius,
                center = Offset(px, py)
            )
        }
    }
}
