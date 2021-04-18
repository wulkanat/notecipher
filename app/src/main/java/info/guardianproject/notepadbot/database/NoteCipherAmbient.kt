package info.guardianproject.notepadbot.database

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject

object NoteCipherAmbient {
    var notes by mutableStateOf(listOf<Note>())

    /**
     * Unlock the database with a given password
     */
    fun unlock(context: Context, password: String): Boolean {
        val database = AppDatabase.getDatabase(context, password) ?: return false
        if (database.isOpen) return true

        GlobalScope.launch {
            val items = database.noteDao().getAll()
            notes = items
        }

        // TODO
        return true
    }

    fun lock(context: Context) {
        AppDatabase.getDatabase(context)?.close()
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        // database?.noteDao()?.changePassphrase(oldPassword, newPassword)
    }

    fun resetDatabase() {

    }
}