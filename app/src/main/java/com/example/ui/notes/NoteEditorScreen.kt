package com.example.ui.notes

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FontRegistry
import com.example.model.NoteCategory
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenDanger
import com.example.ui.theme.SenFieldFill
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteEditorScreen(
    notesViewModel: NotesViewModel,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editingNote by notesViewModel.editingNote.collectAsState()
    val activeFont by walletViewModel.activeFont.collectAsState()

    val note = editingNote ?: return
    val font = FontRegistry.getFontFamily(note.fontKey.takeIf { it != "default" } ?: activeFont)

    var newItemText by remember { mutableStateOf("") }

    // Intercept back button to ensure auto-save & close
    BackHandler {
        notesViewModel.closeEditor()
        onNavigateBack()
    }

    val updatedDateStr = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(note.updatedAt))

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            notesViewModel.closeEditor()
                            onNavigateBack()
                        },
                    shape = CircleShape,
                    color = SenRaisedSurface,
                    border = BorderStroke(1.dp, SenBorderStrong)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Save and Back",
                            tint = SenTextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Chip
                    Surface(
                        shape = CircleShape,
                        color = SenRaisedSurface,
                        border = BorderStroke(1.dp, SenBorderStrong)
                    ) {
                        Text(
                            text = "${note.category.iconEmoji} ${note.category.displayName}",
                            color = SenMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = {
                            notesViewModel.deleteNote(note.id)
                            onNavigateBack()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = SenDanger,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = SenBorderSubtle)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Selector Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NoteCategory.entries.forEach { cat ->
                            val isSelected = note.category == cat
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { notesViewModel.updateEditorCategory(cat) },
                                shape = CircleShape,
                                color = if (isSelected) SenAccentEmerald else SenRaisedSurface,
                                border = BorderStroke(1.dp, if (isSelected) SenBrightEmerald else SenBorderSubtle)
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.displayName}",
                                    color = if (isSelected) SenTextWhite else SenTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Optional Subject field for School Notes
                if (note.category == NoteCategory.SCHOOL) {
                    item {
                        OutlinedTextField(
                            value = note.subject ?: "",
                            onValueChange = { notesViewModel.updateEditorSubject(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Subject / Course (e.g. CS 402)", color = SenTextSecondary, fontSize = 13.sp) },
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
                }

                // Title Input
                item {
                    BasicTextField(
                        value = note.title,
                        onValueChange = { notesViewModel.updateEditorTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = SenTextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        ),
                        cursorBrush = SolidColor(SenBrightEmerald),
                        decorationBox = { innerTextField ->
                            if (note.title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    color = SenTextSecondary.copy(alpha = 0.5f),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = font
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Shopping List Specific UI
                if (note.category == NoteCategory.SHOPPING) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Add item to checklist...", color = SenTextSecondary, fontSize = 14.sp) },
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    notesViewModel.addShoppingItem(newItemText)
                                    newItemText = ""
                                })
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        notesViewModel.addShoppingItem(newItemText)
                                        newItemText = ""
                                    },
                                shape = CircleShape,
                                color = SenAccentEmerald,
                                border = BorderStroke(1.dp, SenBorderStrong)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Item",
                                        tint = SenTextWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(note.shoppingItems, key = { it.id }) { item ->
                        SenCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { notesViewModel.toggleShoppingItem(item.id) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SenAccentEmerald,
                                            checkmarkColor = SenTextWhite,
                                            uncheckedColor = SenTextSecondary
                                        )
                                    )
                                    Text(
                                        text = item.text,
                                        color = if (item.isChecked) SenTextSecondary else SenTextWhite,
                                        fontSize = 15.sp,
                                        fontFamily = font,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }

                                IconButton(
                                    onClick = { notesViewModel.removeShoppingItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = SenTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Note Body
                item {
                    BasicTextField(
                        value = note.body,
                        onValueChange = { notesViewModel.updateEditorBody(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        textStyle = TextStyle(
                            color = SenTextWhite,
                            fontSize = 16.sp,
                            fontFamily = font,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(SenBrightEmerald),
                        decorationBox = { innerTextField ->
                            if (note.body.isEmpty()) {
                                Text(
                                    text = if (note.category == NoteCategory.SHOPPING) "Notes or directions..." else "Start writing your thoughts...",
                                    color = SenTextSecondary.copy(alpha = 0.5f),
                                    fontSize = 16.sp,
                                    fontFamily = font
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Footer Timestamp
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Last updated $updatedDateStr",
                            color = SenTextSecondary.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
