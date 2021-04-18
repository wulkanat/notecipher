package info.guardianproject.notepadbot.database

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    suspend fun getAll(): List<Note>

    @Insert
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)

    // TODO
    /*@Query("""
        PRAGMA key = ':oldPassphrase';
        PRAGMA rekey = ':newPassphrase';
    """)
    fun changePassphrase(oldPassphrase: String, newPassphrase: String): Cursor*/
}