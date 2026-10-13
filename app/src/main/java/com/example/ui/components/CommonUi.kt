package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.formatSenAmount
import com.example.ui.theme.DeepGradientEnd
import com.example.ui.theme.DeepGradientStart
import com.example.ui.theme.EmeraldGradientEnd
import com.example.ui.theme.EmeraldGradientStart
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenCardSurface
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite

enum class HeroGradientStyle {
    DEEP,
    EMERALD
}

/**
 * 28dp rounded hero card with subtle diagonal sheen and 1dp border
 */
@Composable
fun SenHeroCard(
    modifier: Modifier = Modifier,
    gradientStyle: HeroGradientStyle = HeroGradientStyle.DEEP,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val (startColor, endColor) = when (gradientStyle) {
        HeroGradientStyle.DEEP -> DeepGradientStart to DeepGradientEnd
        HeroGradientStyle.EMERALD -> EmeraldGradientStart to EmeraldGradientEnd
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 600f)
                )
            )
            .border(BorderStroke(1.dp, SenBorderSubtle), shape)
            .drawBehind {
                // Subtle diagonal sheen
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.1f, 0f),
                    end = Offset(size.width * 0.9f, size.height),
                    strokeWidth = 32.dp.toPx()
                )
            }
            .padding(20.dp),
        content = content
    )
}

/**
 * 24dp rounded list card with 1dp border at 12% white
 */
@Composable
fun SenCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else Modifier

    Card(
        modifier = modifier
            .then(clickableModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = SenCardSurface),
        border = BorderStroke(1.dp, SenBorderSubtle)
    ) {
        content()
    }
}

/**
 * Press-scale modifier: scales down slightly (0.97f) on press for tactile feel
 */
@Composable
fun Modifier.pressScale(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "press_scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Pill button (54dp high standard) with emerald gradient or outline
 */
@Composable
fun SenPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isOutlined: Boolean = false,
    isDanger: Boolean = false,
    icon: ImageVector? = null,
    height: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "btn_scale"
    )

    val shape = CircleShape

    if (isOutlined) {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = modifier
                .height(height)
                .scale(scale),
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (isDanger) com.example.ui.theme.SenDanger else SenTextWhite
            ),
            border = BorderStroke(
                1.dp,
                if (isDanger) com.example.ui.theme.SenDanger.copy(alpha = 0.6f) else SenBorderStrong
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonInnerContent(text, isLoading, icon, if (isDanger) com.example.ui.theme.SenDanger else SenTextWhite)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = modifier
                .height(height)
                .scale(scale),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDanger) com.example.ui.theme.SenDanger else SenAccentEmerald,
                contentColor = SenTextWhite
            ),
            border = BorderStroke(1.dp, if (isDanger) Color.Transparent else SenBorderStrong),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonInnerContent(text, isLoading, icon, SenTextWhite)
        }
    }
}

@Composable
private fun ButtonInnerContent(
    text: String,
    isLoading: Boolean,
    icon: ImageVector?,
    textColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = SenTextWhite,
            strokeWidth = 2.5.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Coin chip with the balance, opens Coin Store
 */
@Composable
fun CoinChip(
    balance: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = SenRaisedSurface,
        border = BorderStroke(1.dp, SenBorderStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "🪙",
                fontSize = 14.sp
            )
            Text(
                text = formatSenAmount(balance),
                color = SenMint,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "sen",
                color = SenTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Custom Apple logo drawn in code using Compose Canvas and Path (body with bite cutout and leaf).
 * Centered in the circle, tinted white, 22dp size.
 */
@Composable
fun AppleLogoIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(
        modifier = modifier.size(22.dp)
    ) {
        val w = size.width
        val h = size.height

        val applePath = Path().apply {
            // Leaf at top right
            moveTo(w * 0.6231f, h * 0.1809f)
            cubicTo(w * 0.6611f, h * 0.1330f, w * 0.6877f, h * 0.0669f, w * 0.6801f, h * 0.0000f)
            cubicTo(w * 0.6239f, h * 0.0023f, w * 0.5547f, h * 0.0380f, w * 0.5152f, h * 0.0851f)
            cubicTo(w * 0.4802f, h * 0.1261f, w * 0.4491f, h * 0.1930f, w * 0.4574f, h * 0.2576f)
            cubicTo(w * 0.5213f, h * 0.2629f, w * 0.5859f, h * 0.2257f, w * 0.6231f, h * 0.1809f)
            close()

            // Apple body with bite cutout
            moveTo(w * 0.6793f, h * 0.3822f)
            cubicTo(w * 0.6125f, h * 0.3784f, w * 0.5555f, h * 0.4210f, w * 0.5236f, h * 0.4210f)
            cubicTo(w * 0.4909f, h * 0.4210f, w * 0.4430f, h * 0.3845f, w * 0.3906f, h * 0.3853f)
            cubicTo(w * 0.3222f, h * 0.3860f, w * 0.2584f, h * 0.4255f, w * 0.2234f, h * 0.4871f)
            cubicTo(w * 0.1512f, h * 0.6117f, w * 0.2052f, h * 0.7964f, w * 0.2743f, h * 0.8967f)
            cubicTo(w * 0.3085f, h * 0.9460f, w * 0.3488f, h * 1.0000f, w * 0.4020f, h * 0.9985f)
            cubicTo(w * 0.4536f, h * 0.9962f, w * 0.4726f, h * 0.9650f, w * 0.5350f, h * 0.9650f)
            cubicTo(w * 0.5973f, h * 0.9650f, w * 0.6147f, h * 0.9985f, w * 0.6687f, h * 0.9977f)
            cubicTo(w * 0.7242f, h * 0.9970f, w * 0.7591f, h * 0.9483f, w * 0.7926f, h * 0.8989f)
            cubicTo(w * 0.8321f, h * 0.8419f, w * 0.8480f, h * 0.7865f, w * 0.8488f, h * 0.7834f)
            cubicTo(w * 0.8473f, h * 0.7827f, w * 0.7409f, h * 0.7416f, w * 0.7401f, h * 0.6185f)
            cubicTo(w * 0.7394f, h * 0.5160f, w * 0.8237f, h * 0.4666f, w * 0.8275f, h * 0.4643f)
            cubicTo(w * 0.7796f, h * 0.3936f, w * 0.7044f, h * 0.3853f, w * 0.6786f, h * 0.3837f)
            close()
        }

        drawPath(applePath, color = tint)
    }
}

/**
 * Circular social auth button (52dp, rounded circle, raised surface)
 */
@Composable
fun SocialCircleButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = SenRaisedSurface,
        border = BorderStroke(1.dp, SenBorderStrong)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Three circular social buttons: Google ("G"), Apple (Canvas Apple logo), Facebook ("f").
 * All three icons have the same visual size, weight, and 52dp circular touch target.
 */
@Composable
fun SocialButtonsRow(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Google button
        SocialCircleButton(
            label = "Google",
            onClick = onGoogleClick
        ) {
            Text(
                text = "G",
                color = SenTextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Apple button (apple logo drawn in code, 22dp, white)
        SocialCircleButton(
            label = "Apple",
            onClick = onAppleClick
        ) {
            AppleLogoIcon(
                modifier = Modifier.size(22.dp),
                tint = SenTextWhite
            )
        }

        // Facebook button
        SocialCircleButton(
            label = "Facebook",
            onClick = onFacebookClick
        ) {
            Text(
                text = "f",
                color = SenTextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

