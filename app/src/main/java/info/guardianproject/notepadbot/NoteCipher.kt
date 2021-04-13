/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")savedInstanceState;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.guardianproject.notepadbot

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import info.guardianproject.notepadbot.NoteEdit.NoteContentLoader
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import net.sqlcipher.database.SQLiteDatabase
import java.io.IOException

class NoteCipher : AppCompatActivity(), ICacheWordSubscriber {
    private var mDbHelper: NotesDbAdapter? = null
    private var dataStream: Uri? = null
    private var mCacheWord: CacheWordActivityHandler? = null
    private var notesListView: ListView? = null
    private var notesCursorAdapter: SimpleCursorAdapter? = null

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            dataStream = if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                intent.extras!![Intent.EXTRA_STREAM] as Uri?
            } else {
                intent.data
            }
        }
        SQLiteDatabase.loadLibs(this)
        setContentView(R.layout.notes_list)
        notesListView = findViewById<View>(R.id.notesListView) as ListView
        notesListView!!.onItemClickListener =
            OnItemClickListener { _, _, _, id ->
                val i = Intent(application, NoteEdit::class.java)
                i.putExtra(NotesDbAdapter.KEY_ROWID, id)
                startActivityForResult(i, ACTIVITY_EDIT)
            }
        registerForContextMenu(notesListView)
        mCacheWord = CacheWordActivityHandler(this, (application as App).cWSettings)

        // Create an array to specify the fields we want to display in the list (only TITLE)
        val from = arrayOf(NotesDbAdapter.KEY_TITLE)

        // and an array of the fields we want to bind those fields to (in this
        // case just text1)
        val to = intArrayOf(R.id.row_text)

        // Now create an empty simple cursor adapter that later will display the notes
        notesCursorAdapter = SimpleCursorAdapter(
            this, R.layout.notes_row, null, from, to,
            SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        notesListView!!.adapter = notesCursorAdapter
    }

    override fun onPause() {
        super.onPause()
        mCacheWord!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        mCacheWord!!.onResume()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewById<View>(R.id.listlayout).setOnTouchListener { v, _ ->
            if (mDbHelper != null && mDbHelper!!.isOpen) createNote()
            v.performClick()
            false
        }
    }

    private fun closeDatabase() {
        mDbHelper?.let {
            it.close()
            mDbHelper = null
        }
    }

    private fun unlockDatabase() {
        if (mCacheWord!!.isLocked) return
        mDbHelper = NotesDbAdapter(mCacheWord, this)
        try {
            mDbHelper!!.open()
            if (dataStream != null) importDataStream() else fillData()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.err_pass), Toast.LENGTH_LONG).show()
        }
    }

    private fun fillData() {
        if (mCacheWord!!.isLocked) return
        supportLoaderManager.restartLoader(
            VIEW_ID,
            null,
            object : LoaderManager.LoaderCallbacks<Cursor> {
                override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
                    return NotesLoader(this@NoteCipher, mDbHelper)
                }

                override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
                    notesCursorAdapter!!.swapCursor(cursor)
                    val emptyTV = findViewById<View>(R.id.emptytext) as TextView
                    if (notesCursorAdapter!!.isEmpty) {
                        Toast.makeText(this@NoteCipher, R.string.on_start, Toast.LENGTH_LONG).show()
                        emptyTV.setText(R.string.no_notes)
                    } else {
                        emptyTV.text = ""
                    }
                }

                override fun onLoaderReset(loader: Loader<Cursor>) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menu.add(0, INSERT_ID, 0, R.string.menu_insert).apply {
            setIcon(R.drawable.new_content)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu.add(0, LOCK_ID, 0, R.string.menu_lock).apply {
            setIcon(R.drawable.lock)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu.add(0, SETTINGS_ID, 0, R.string.settings).apply {
            setIcon(R.drawable.settings)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            INSERT_ID -> {
                createNote()
                return true
            }
            RE_KEY_ID -> return true
            LOCK_ID -> {
                if (!mCacheWord!!.isLocked) mCacheWord!!.manuallyLock()
                return true
            }
            SETTINGS_ID -> {
                if (!mCacheWord!!.isLocked) {
                    startActivity(Intent(this, Settings::class.java))
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, SHARE_ID, 0, R.string.menu_share)
        menu.add(0, DELETE_ID, 0, R.string.menu_delete)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info: AdapterContextMenuInfo
        when (item.itemId) {
            DELETE_ID -> {
                info = item.menuInfo as AdapterContextMenuInfo
                mDbHelper!!.deleteNote(info.id)
                fillData()
                return true
            }
            SHARE_ID -> {
                info = item.menuInfo as AdapterContextMenuInfo
                shareEntry(info.id)
                return true
            }
            VIEW_ID -> {
                info = item.menuInfo as AdapterContextMenuInfo
                viewEntry(info.id)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun shareEntry(id: Long) {
        if (mCacheWord!!.isLocked) {
            return
        }
        supportLoaderManager.restartLoader(
            SHARE_ID,
            null,
            object : LoaderManager.LoaderCallbacks<Cursor> {
                override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
                    return NoteContentLoader(this@NoteCipher, mDbHelper, id)
                }

                override fun onLoadFinished(loader: Loader<Cursor>, note: Cursor) {
                    val blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA))
                    val title = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE))
                    var mimeType =
                        note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE))
                    if (mimeType == null) mimeType = "text/plain"
                    if (blob != null) {
                        try {
                            NoteUtils.shareData(this@NoteCipher, title, mimeType, blob)
                        } catch (e: IOException) {
                            Toast.makeText(
                                this@NoteCipher,
                                getString(R.string.err_export, e.message),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    } else {
                        val body =
                            note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY))
                        NoteUtils.shareText(this@NoteCipher, body)
                    }
                    note.close()
                }

                override fun onLoaderReset(loader: Loader<Cursor>) {}
            })
    }

    private fun viewEntry(id: Long) {
        if (mCacheWord!!.isLocked) return
        supportLoaderManager.restartLoader(
            VIEW_ID,
            null,
            object : LoaderManager.LoaderCallbacks<Cursor> {
                override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
                    return NoteContentLoader(this@NoteCipher, mDbHelper, id)
                }

                override fun onLoadFinished(loader: Loader<Cursor>, note: Cursor) {
                    val blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA))
                    var mimeType =
                        note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE))
                    if (mimeType == null) mimeType = "text/plain"
                    if (blob != null) {
                        val title = note.getString(
                            note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)
                        )
                        NoteUtils.savePublicFile(this@NoteCipher, title, mimeType, blob)
                    }
                    note.close()
                }

                override fun onLoaderReset(loader: Loader<Cursor>) {}
            })
    }

    private fun createNote() {
        if (mCacheWord!!.isLocked) return
        val i = Intent(this, NoteEdit::class.java)
        startActivityForResult(i, ACTIVITY_CREATE)
    }

    /*
     * Called after the return from creating a new note (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        mDbHelper = NotesDbAdapter(mCacheWord, this)
        fillData()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDatabase()
        NoteUtils.cleanupTmp(this)
    }

    private fun importDataStream() {
        if (mCacheWord!!.isLocked) return
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(dataStream!!)
            val mimeType = contentResolver.getType(dataStream!!)
            val data = NoteUtils.readBytesAndClose(inputStream)
            if (data!!.size > NConstants.MAX_STREAM_SIZE) {
                Toast.makeText(this, R.string.err_size, Toast.LENGTH_LONG).show()
            } else {
                val title = dataStream!!.lastPathSegment
                val body = dataStream!!.path
                NotesDbAdapter(mCacheWord, this).createNote(title, body, data, mimeType)
                Toast.makeText(this, getString(R.string.on_import, title), Toast.LENGTH_LONG).show()

                // handleDelete();
                dataStream = null
                System.gc()
                fillData()
            }
        } catch (e: IOException) {
            Log.e(NConstants.TAG, e.message, e)
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, R.string.err_size, Toast.LENGTH_LONG).show()
        } finally {
            dataStream = null
        }
    }

    /*
     * Call this to delete the original image, will ask the user
     */
    private fun handleDelete() {
        if (mCacheWord!!.isLocked) return

        AlertDialog.Builder(this).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setTitle(R.string.app_name)
            setMessage(R.string.confirm_delete)
            setPositiveButton(android.R.string.yes) { _, _ -> // User clicked OK so go ahead and delete
                contentResolver?.delete(dataStream!!, null, null)
                    ?: Toast.makeText(
                        this@NoteCipher,
                        R.string.unable_to_delete_original,
                        Toast.LENGTH_SHORT
                    ).show()
            }
            setNegativeButton(android.R.string.no) { _, _ -> }
        }.show()
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("originalIntent", getIntent())
        startActivity(intent)
        finish()
    }

    private fun clearViewsAndLock() {
        closeDatabase()
        notesListView!!.adapter = null
        System.gc()
        showLockScreen()
    }

    override fun onCacheWordUninitialized() {
        Log.d(NConstants.TAG, "onCacheWordUninitialized")
        clearViewsAndLock()
    }

    override fun onCacheWordLocked() {
        Log.d(NConstants.TAG, "onCacheWordLocked")
        clearViewsAndLock()
    }

    override fun onCacheWordOpened() {
        Log.d(NConstants.TAG, "onCacheWordOpened")
        unlockDatabase()
        if (mDbHelper!!.isOpen) {
            if (dataStream != null) importDataStream() else fillData()
        }
    }

    class NotesLoader(context: Context?, var db: NotesDbAdapter?) : CursorLoader(context!!) {
        override fun loadInBackground(): Cursor? {
            return db?.fetchAllNotes()
        }
    }

    companion object {
        private const val ACTIVITY_CREATE = 0
        private const val ACTIVITY_EDIT = 1
        private const val INSERT_ID = Menu.FIRST
        private const val DELETE_ID = Menu.FIRST + 1
        private const val RE_KEY_ID = Menu.FIRST + 2
        private const val SHARE_ID = Menu.FIRST + 3
        private const val VIEW_ID = Menu.FIRST + 4
        private const val LOCK_ID = Menu.FIRST + 5
        private const val SETTINGS_ID = Menu.FIRST + 6
    }
}