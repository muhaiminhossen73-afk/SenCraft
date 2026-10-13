package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite

enum class MainNavTab(val title: String) {
    COIN_STORE("Coin Store"),
    HOME("Home"),
    PROFILE("Profile")
}

@Composable
fun FloatingBottomBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating pill bar container with no clipping so Home button rises above
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 16.dp, shape = CircleShape, clip = false, spotColor = Color.Black)
                .background(SenRaisedSurface, CircleShape)
                .border(BorderStroke(1.dp, SenBorderStrong), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Coin Store
                NavPillItem(
                    label = "Coin Store",
                    selectedIcon = Icons.Filled.Paid,
                    unselectedIcon = Icons.Outlined.Paid,
                    isSelected = currentTab == MainNavTab.COIN_STORE,
                    onClick = { onTabSelected(MainNavTab.COIN_STORE) }
                )

                // 2. Home (Raised circular emerald button with soft glow in center)
                HomeNavButton(
                    isSelected = currentTab == MainNavTab.HOME,
                    onClick = { onTabSelected(MainNavTab.HOME) }
                )

                // 3. Profile
                NavPillItem(
                    label = "Profile",
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                    isSelected = currentTab == MainNavTab.PROFILE,
                    onClick = { onTabSelected(MainNavTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) SenAccentEmerald else Color.Transparent,
        animationSpec = spring(stiffness = 300f),
        label = "nav_pill_color"
    )

    Box(
        modifier = Modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 10.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (isSelected) SenTextWhite else SenTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            // Animated expansion showing label on one line
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = label,
                        color = SenTextWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeNavButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .offset(y = (-8).dp)
            .size(58.dp)
            .drawBehind {
                // Soft glow effect around raised center button
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SenBrightEmerald.copy(alpha = if (isSelected) 0.55f else 0.35f),
                            SenAccentEmerald.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SenBrightEmerald, SenAccentEmerald)
                )
            )
            .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "Home",
            tint = SenTextWhite,
            modifier = Modifier.size(26.dp)
        )
    }
}
