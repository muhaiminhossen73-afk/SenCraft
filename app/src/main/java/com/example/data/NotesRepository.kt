package com.example.data

import com.example.model.Note
import com.example.model.NoteCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface NotesRepository {
    val allNotesFlow: Flow<List<Note>>
    val favoriteNotesFlow: Flow<List<Note>>
    fun getNotesByCategory(category: NoteCategory): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?>
    suspend fun upsertNote(note: Note)
    suspend fun deleteNote(id: String): Note?
    suspend fun restoreNote(note: Note)
    suspend fun toggleFavorite(id: String)
    suspend fun clearAllNotes()
}

class DefaultNotesRepository(
    private val dataStoreManager: DataStoreManager
) : NotesRepository {

    override val allNotesFlow: Flow<List<Note>> = dataStoreManager.notesFlow

    override val favoriteNotesFlow: Flow<List<Note>> = dataStoreManager.notesFlow.map { list ->
        list.filter { it.isFavorite }
    }

    override fun getNotesByCategory(category: NoteCategory): Flow<List<Note>> {
        return dataStoreManager.notesFlow.map { list ->
            list.filter { it.category == category }
        }
    }

    override fun getNoteById(id: String): Flow<Note?> {
        return dataStoreManager.notesFlow.map { list ->
            list.find { it.id == id }
        }
    }

    override suspend fun upsertNote(note: Note) {
        val current = dataStoreManager.notesFlow.first()
        val index = current.indexOfFirst { it.id == note.id }
        val updated = current.toMutableList()
        val noteToSave = note.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            updated[index] = noteToSave
        } else {
            updated.add(0, noteToSave)
        }
        dataStoreManager.saveNotes(updated)
    }

    override suspend fun deleteNote(id: String): Note? {
        val current = dataStoreManager.notesFlow.first()
        val toDelete = current.find { it.id == id }
        if (toDelete != null) {
            val updated = current.filterNot { it.id == id }
            dataStoreManager.saveNotes(updated)
        }
        return toDelete
    }

    override suspend fun restoreNote(note: Note) {
        val current = dataStoreManager.notesFlow.first()
        val updated = current.toMutableList()
        updated.add(0, note)
        dataStoreManager.saveNotes(updated)
    }

    override suspend fun toggleFavorite(id: String) {
        val current = dataStoreManager.notesFlow.first()
        val updated = current.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        dataStoreManager.saveNotes(updated)
    }

    override suspend fun clearAllNotes() {
        dataStoreManager.clearAllNotes()
    }
}
