package info.guardianproject.notepadbot.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {
    // val allNotes: Flow<List<Note>> = noteDao.getAll()

    @WorkerThread
    suspend fun insert(note: Note) {
         // noteDao.insert(note)
    }
}