package com.example.ui.notes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Note
import com.example.model.NoteCategory
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.home.RecentNoteCard
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenDanger
import com.example.ui.theme.SenFieldFill
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CategoryNotesScreen(
    category: NoteCategory,
    notesViewModel: NotesViewModel,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onOpenNoteEditor: (Note?) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by notesViewModel.searchQuery.collectAsState()
    val allNotes by notesViewModel.allNotes.collectAsState()
    val activeFont by walletViewModel.activeFont.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(category) {
        notesViewModel.selectCategory(category)
    }

    LaunchedEffect(Unit) {
        notesViewModel.snackbarEvent.collectLatest { (message, undoAction) ->
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = if (undoAction != null) "Undo" else null,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    undoAction?.invoke()
                }
            }
        }
    }

    val categoryNotes = allNotes.filter { it.category == category }
    val displayedNotes = if (searchQuery.isBlank()) {
        categoryNotes
    } else {
        val q = searchQuery.trim().lowercase()
        categoryNotes.filter {
            it.title.lowercase().contains(q) ||
            it.body.lowercase().contains(q) ||
            (it.subject != null && it.subject.lowercase().contains(q)) ||
            it.shoppingItems.any { item -> item.text.lowercase().contains(q) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    notesViewModel.startNewNote(category)
                    onOpenNoteEditor(null)
                },
                containerColor = SenBrightEmerald,
                contentColor = SenTextWhite,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 90.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New ${category.displayName}",
                    tint = Color(0xFF050B08),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.iconEmoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.displayName,
                                    color = SenTextWhite,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${categoryNotes.size} saved",
                                color = SenTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Search field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { notesViewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search ${category.displayName.lowercase()}...", color = SenTextSecondary, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = SenTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { notesViewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = SenTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
                        )
                    )
                }

                if (displayedNotes.isEmpty()) {
                    item {
                        SenCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = category.iconEmoji,
                                    fontSize = 44.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No matching results" else "No ${category.displayName.lowercase()} yet",
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the '+' button to add one.",
                                    color = SenTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(displayedNotes, key = { it.id }) { note ->
                        Box(modifier = Modifier.fillMaxWidth()) {
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
    }
}
