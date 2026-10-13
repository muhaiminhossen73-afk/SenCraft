package com.example.ui.notes

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Note
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.home.RecentNoteCard
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarViewScreen(
    notesViewModel: NotesViewModel,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onOpenNoteEditor: (Note?) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendarMonth by notesViewModel.calendarMonth.collectAsState()
    val selectedDate by notesViewModel.selectedCalendarDate.collectAsState()
    val activeFont by walletViewModel.activeFont.collectAsState()

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthFormat.format(calendarMonth.time)

    // Calculate calendar grid days
    val cal = (calendarMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selectedDayNotes = if (selectedDate != null) {
        notesViewModel.getNotesForDate(selectedDate!!)
    } else emptyList()

    val selectedDateStr = if (selectedDate != null) {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate!!)
    } else "No day selected"

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNavigateBack),
                        shape = CircleShape,
                        color = SenRaisedSurface,
                        border = BorderStroke(1.dp, SenBorderStrong)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SenTextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Calendar View",
                            color = SenTextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Browse notes by timeline",
                            color = SenTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Month navigation card
            item {
                SenCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Month selector bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { notesViewModel.prevMonth() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = SenTextWhite
                                )
                            }

                            Text(
                                text = monthTitle,
                                color = SenTextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { notesViewModel.nextMonth() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = SenTextWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Day headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayHeader ->
                                Text(
                                    text = dayHeader,
                                    color = SenMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month days grid (weeks)
                        val totalCells = (firstDayOfWeek - 1) + daysInMonth
                        val numRows = (totalCells + 6) / 7

                        for (row in 0 until numRows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..6) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - (firstDayOfWeek - 1) + 1

                                    if (dayNum in 1..daysInMonth) {
                                        val dayCal = (calendarMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayNum)
                                        }
                                        val hasNotes = notesViewModel.hasNotesOnDay(
                                            day = dayNum,
                                            month = calendarMonth.get(Calendar.MONTH),
                                            year = calendarMonth.get(Calendar.YEAR)
                                        )

                                        val isSelected = selectedDate != null && run {
                                            val sCal = Calendar.getInstance().apply { time = selectedDate!! }
                                            sCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                            sCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) SenAccentEmerald else Color.Transparent
                                                )
                                                .clickable {
                                                    notesViewModel.selectCalendarDate(dayCal.time)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    color = if (isSelected) SenTextWhite else SenTextSecondary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || hasNotes) FontWeight.Bold else FontWeight.Normal
                                                )

                                                if (hasNotes) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(SenMint, CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateStr,
                        color = SenTextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedDayNotes.size} notes",
                        color = SenMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Notes list for selected day
            if (selectedDayNotes.isEmpty()) {
                item {
                    SenCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📅", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No notes on this day",
                                color = SenTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Days with notes are marked with a green mint dot.",
                                color = SenTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(selectedDayNotes, key = { it.id }) { note ->
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
