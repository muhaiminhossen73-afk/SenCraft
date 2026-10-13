package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FontRegistry
import com.example.model.Note
import com.example.model.NoteCategory
import com.example.model.User
import com.example.ui.components.CoinChip
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.notes.NotesViewModel
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.DeepGradientEnd
import com.example.ui.theme.DeepGradientStart
import com.example.ui.theme.EmeraldGradientEnd
import com.example.ui.theme.EmeraldGradientStart
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    user: User?,
    walletViewModel: WalletViewModel,
    notesViewModel: NotesViewModel,
    onOpenCoinStore: () -> Unit,
    onOpenNoteEditor: (Note?) -> Unit,
    onOpenCategory: (NoteCategory) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by walletViewModel.balance.collectAsState()
    val allNotes by notesViewModel.allNotes.collectAsState()
    val activeFont by walletViewModel.activeFont.collectAsState()

    val firstName = user?.name?.split(" ")?.firstOrNull() ?: "Scholar"
    val recentNotes = allNotes.take(3)

    val normalCount = allNotes.count { it.category == NoteCategory.NORMAL }
    val shoppingCount = allNotes.count { it.category == NoteCategory.SHOPPING }
    val schoolCount = allNotes.count { it.category == NoteCategory.SCHOOL }

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Greeting + Coin Chip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, $firstName",
                            color = SenTextWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome to your luxury sanctuary",
                            color = SenTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    CoinChip(
                        balance = balance,
                        onClick = onOpenCoinStore
                    )
                }
            }

            // Quick Actions Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionPill(
                        icon = Icons.Filled.Notes,
                        label = "New note",
                        onClick = {
                            notesViewModel.startNewNote(NoteCategory.NORMAL)
                            onOpenNoteEditor(null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionPill(
                        icon = Icons.Filled.FormatListBulleted,
                        label = "New list",
                        onClick = {
                            notesViewModel.startNewNote(NoteCategory.SHOPPING)
                            onOpenNoteEditor(null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionPill(
                        icon = Icons.Filled.CalendarMonth,
                        label = "Calendar",
                        onClick = onOpenCalendar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Note Category Pills (from Notes entry points)
            // 1. Normal Notes
            item {
                HomeCategoryPillCard(
                    title = "Normal Notes",
                    subtitle = "$normalCount items",
                    emoji = "📝",
                    gradientColors = listOf(DeepGradientStart, DeepGradientEnd),
                    onClick = { onOpenCategory(NoteCategory.NORMAL) }
                )
            }

            // 2. Shopping List
            item {
                HomeCategoryPillCard(
                    title = "Shopping List",
                    subtitle = "$shoppingCount lists",
                    emoji = "🛒",
                    gradientColors = listOf(EmeraldGradientStart, EmeraldGradientEnd),
                    onClick = { onOpenCategory(NoteCategory.SHOPPING) }
                )
            }

            // 3. School Notes
            item {
                HomeCategoryPillCard(
                    title = "School Notes",
                    subtitle = "$schoolCount subjects",
                    emoji = "🎓",
                    gradientColors = listOf(Color(0xFF1B5E45), Color(0xFF124230)),
                    onClick = { onOpenCategory(NoteCategory.SCHOOL) }
                )
            }

            // Calendar View card (darker card)
            item {
                SenCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    onClick = onOpenCalendar
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = SenRaisedSurface,
                                border = BorderStroke(1.dp, SenBorderStrong)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = SenMint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Calendar View",
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Browse notes by date",
                                    color = SenTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open",
                            tint = SenTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Recent Notes Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent notes",
                        color = SenTextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (recentNotes.isNotEmpty()) {
                        Text(
                            text = "${allNotes.size} total",
                            color = SenMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (recentNotes.isEmpty()) {
                item {
                    SenCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✍️",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No notes yet",
                                color = SenTextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap New note to start capturing your thoughts.",
                                color = SenTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(recentNotes, key = { it.id }) { note ->
                    RecentNoteCard(
                        note = note,
                        globalFontKey = activeFont,
                        onClick = {
                            notesViewModel.editNote(note)
                            onOpenNoteEditor(note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryPillCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, SenBorderSubtle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            color = SenTextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = SenTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = SenTextWhite.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = SenRaisedSurface,
        border = BorderStroke(1.dp, SenBorderStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SenMint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = SenTextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RecentNoteCard(
    note: Note,
    globalFontKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val font = FontRegistry.getFontFamily(note.fontKey.takeIf { it != "default" } ?: globalFontKey)
    val formattedDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.updatedAt))

    SenCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.category.iconEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        color = SenTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val previewText = when (note.category) {
                NoteCategory.SHOPPING -> {
                    if (note.shoppingItems.isEmpty()) "Empty shopping list"
                    else note.shoppingItems.joinToString(", ") { (if (it.isChecked) "✓ " else "○ ") + it.text }
                }
                NoteCategory.SCHOOL -> {
                    val sub = if (!note.subject.isNullOrBlank()) "[${note.subject}] " else ""
                    sub + note.body.ifBlank { "No notes content" }
                }
                NoteCategory.NORMAL -> note.body.ifBlank { "No content" }
            }

            Text(
                text = previewText,
                color = SenTextSecondary,
                fontSize = 13.sp,
                fontFamily = font,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.category.displayName,
                    color = SenBrightEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formattedDate,
                    color = SenTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
