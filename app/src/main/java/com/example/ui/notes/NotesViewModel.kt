package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NotesRepository
import com.example.model.Note
import com.example.model.NoteCategory
import com.example.model.ShoppingItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = notesRepository.allNotesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteNotes: StateFlow<List<Note>> = notesRepository.favoriteNotesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<NoteCategory?>(null)
    val selectedCategory: StateFlow<NoteCategory?> = _selectedCategory.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(allNotes, _searchQuery, _selectedCategory) { notes, query, category ->
        var res = notes
        if (category != null) {
            res = res.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            res = res.filter {
                it.title.lowercase().contains(q) ||
                it.body.lowercase().contains(q) ||
                (it.subject != null && it.subject.lowercase().contains(q)) ||
                it.shoppingItems.any { item -> item.text.lowercase().contains(q) }
            }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Editor state
    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    private val _lastDeletedNote = MutableStateFlow<Note?>(null)

    private val _snackbarEvent = MutableSharedFlow<Pair<String, (() -> Unit)?>>()
    val snackbarEvent: SharedFlow<Pair<String, (() -> Unit)?>> = _snackbarEvent.asSharedFlow()

    // Calendar state
    private val _calendarMonth = MutableStateFlow(Calendar.getInstance())
    val calendarMonth: StateFlow<Calendar> = _calendarMonth.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow<Date?>(Date())
    val selectedCalendarDate: StateFlow<Date?> = _selectedCalendarDate.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: NoteCategory?) {
        _selectedCategory.value = category
    }

    fun startNewNote(category: NoteCategory = NoteCategory.NORMAL) {
        val newNote = Note(
            id = "note_${UUID.randomUUID()}",
            title = "",
            body = "",
            category = category,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _editingNote.value = newNote
    }

    fun editNote(note: Note) {
        _editingNote.value = note
    }

    fun closeEditor() {
        val note = _editingNote.value
        if (note != null && (note.title.isNotBlank() || note.body.isNotBlank() || note.shoppingItems.isNotEmpty())) {
            viewModelScope.launch {
                notesRepository.upsertNote(note)
            }
        }
        _editingNote.value = null
    }

    fun updateEditorTitle(newTitle: String) {
        _editingNote.value = _editingNote.value?.copy(title = newTitle)
        autoSave()
    }

    fun updateEditorBody(newBody: String) {
        _editingNote.value = _editingNote.value?.copy(body = newBody)
        autoSave()
    }

    fun updateEditorSubject(newSubject: String) {
        _editingNote.value = _editingNote.value?.copy(subject = newSubject)
        autoSave()
    }

    fun updateEditorCategory(newCategory: NoteCategory) {
        _editingNote.value = _editingNote.value?.copy(category = newCategory)
        autoSave()
    }

    fun toggleEditorFavorite() {
        val cur = _editingNote.value ?: return
        val updated = cur.copy(isFavorite = !cur.isFavorite)
        _editingNote.value = updated
        autoSave()
    }

    fun addShoppingItem(text: String) {
        if (text.isBlank()) return
        val cur = _editingNote.value ?: return
        val newItem = ShoppingItem(id = "item_${UUID.randomUUID()}", text = text.trim(), isChecked = false)
        val updatedList = cur.shoppingItems + newItem
        _editingNote.value = cur.copy(shoppingItems = updatedList)
        autoSave()
    }

    fun toggleShoppingItem(itemId: String) {
        val cur = _editingNote.value ?: return
        val updatedList = cur.shoppingItems.map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        }
        _editingNote.value = cur.copy(shoppingItems = updatedList)
        autoSave()
    }

    fun removeShoppingItem(itemId: String) {
        val cur = _editingNote.value ?: return
        val updatedList = cur.shoppingItems.filterNot { it.id == itemId }
        _editingNote.value = cur.copy(shoppingItems = updatedList)
        autoSave()
    }

    private fun autoSave() {
        val note = _editingNote.value ?: return
        if (note.title.isNotBlank() || note.body.isNotBlank() || note.shoppingItems.isNotEmpty()) {
            viewModelScope.launch {
                notesRepository.upsertNote(note)
            }
        }
    }

    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            notesRepository.toggleFavorite(noteId)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val deleted = notesRepository.deleteNote(noteId)
            if (deleted != null) {
                _lastDeletedNote.value = deleted
                if (_editingNote.value?.id == noteId) {
                    _editingNote.value = null
                }
                _snackbarEvent.emit(
                    Pair("Note deleted") {
                        viewModelScope.launch {
                            notesRepository.restoreNote(deleted)
                        }
                    }
                )
            }
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            notesRepository.clearAllNotes()
            _snackbarEvent.emit(Pair("All notes cleared", null))
        }
    }

    // Calendar navigation
    fun nextMonth() {
        val cal = (_calendarMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        _calendarMonth.value = cal
    }

    fun prevMonth() {
        val cal = (_calendarMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        _calendarMonth.value = cal
    }

    fun selectCalendarDate(date: Date) {
        _selectedCalendarDate.value = date
    }

    fun getNotesForDate(date: Date): List<Note> {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance()
        return allNotes.value.filter { note ->
            cal2.timeInMillis = note.createdAt
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }

    fun hasNotesOnDay(day: Int, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        return allNotes.value.any { note ->
            cal.timeInMillis = note.createdAt
            cal.get(Calendar.DAY_OF_MONTH) == day &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.YEAR) == year
        }
    }
}
