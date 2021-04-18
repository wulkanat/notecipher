package info.guardianproject.notepadbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private const val DATABASE_NAME = "app_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, password: String? = null): AppDatabase? {
            return INSTANCE ?: password?.let {
                synchronized(this) {
                    val factory = SupportFactory(SQLiteDatabase.getBytes(it.toCharArray()))
                    // TODO: clear password string, the char array will be cleared automatically

                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME,
                    ).openHelperFactory(factory).build()
                }
            }
        }
    }
}