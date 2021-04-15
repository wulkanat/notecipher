/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package info.guardianproject.notepadbot

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import info.guardianproject.notepadbot.cacheword.CacheWordHandler
import info.guardianproject.notepadbot.cacheword.SQLCipherOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import kotlin.jvm.Throws

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 *
 * Constructor - takes the context to allow the database to be
 * opened/created
 *
 * @param ctx the Context within which to work
 */
class NotesDbAdapter(private val mCacheWord: CacheWordHandler, private val mCtx: Context) {
    private var mDbHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(cacheWord: CacheWordHandler?, context: Context?) :
        SQLCipherOpenHelper(cacheWord, context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data")
            if (oldVersion == 2) {
                db.execSQL("ALTER TABLE notes ADD $KEY_DATA blog")
                db.execSQL("ALTER TABLE notes ADD $KEY_TYPE text")
            }
            if (newVersion == 3) {
                db.execSQL("ALTER TABLE notes ADD $KEY_TYPE text")
            }
            //need to migrate old notes here
            //  db.execSQL("DROP TABLE IF EXISTS notes");
            //onCreate(db);
        }
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    @Throws(SQLException::class)
    fun open(): NotesDbAdapter {
        Log.e(TAG, "opening with cacheword")
        mDbHelper = DatabaseHelper(mCacheWord, mCtx)
        mDb = mDbHelper!!.writableDatabase
        System.gc()
        return this
    }

    val isOpen: Boolean
        get() = if (mDb != null) mDb!!.isOpen else false

    fun rekey(password: String) {
        mDb!!.execSQL("PRAGMA rekey = '$password'")
        System.gc()
    }

    fun close() {
        if (mDbHelper != null) mDbHelper!!.close()
        if (mDb != null && mDb!!.isOpen) mDb!!.close()
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    fun createNote(
        title: String?,
        body: String?,
        data: ByteArray?,
        dataType: String?
    ): Long {
        openGuard()
        val initialValues = ContentValues()
        initialValues.put(KEY_TITLE, title)
        initialValues.put(KEY_BODY, body)
        if (data != null) {
            initialValues.put(KEY_DATA, data)
            initialValues.put(KEY_TYPE, dataType)
        }
        return if (mDb != null) mDb!!.insert(
            DATABASE_TABLE,
            null,
            initialValues
        ) else -1
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    fun deleteNote(rowId: Long): Boolean {
        openGuard()
        return mDb!!.delete(
            DATABASE_TABLE,
            "$KEY_ROW_ID=$rowId",
            null
        ) > 0
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    @Throws(SQLException::class)
    fun fetchAllNotes(): Cursor {
        openGuard()
        return mDb!!.query(
            DATABASE_TABLE,
            arrayOf(
                KEY_ROW_ID,
                KEY_TITLE
            ),
            null,
            null,
            null,
            null,
            null
        )
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    @Throws(SQLException::class)
    fun fetchNote(rowId: Long): Cursor? {
        openGuard()
        return mDb?.query(
            true,
            DATABASE_TABLE,
            arrayOf(KEY_ROW_ID, KEY_TITLE, KEY_BODY, KEY_DATA, KEY_TYPE),
            "$KEY_ROW_ID=$rowId", null,
            null, null, null, null
        )?.apply { moveToFirst() }
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    fun updateNote(rowId: Long, title: String?, body: String?, data: ByteArray?, dataType: String?): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(KEY_TITLE, title)
        args.put(KEY_BODY, body)
        if (data != null) {
            args.put(KEY_DATA, data)
            args.put(KEY_DATA, dataType)
        }
        return mDb!!.update(DATABASE_TABLE, args, "$KEY_ROW_ID=$rowId", null) > 0
    }

    @Throws(SQLiteException::class)
    private fun openGuard() {
        if (isOpen) return
        open()
        if (isOpen) return
        Log.d(TAG, "open guard failed")
        throw SQLiteException("Could not open database")
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_DATA = "odata"
        const val KEY_TYPE = "otype"
        const val KEY_ROW_ID = "_id"
        private const val TAG = "NotesDbAdapter"

        /**
         * Database creation sql statement
         */
        private const val DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, $KEY_TITLE text not null, $KEY_BODY text not null, $KEY_DATA blob,$KEY_TYPE text);"
        private const val DATABASE_NAME = "data"
        private const val DATABASE_TABLE = "notes"
        private const val DATABASE_VERSION = 4
    }

}