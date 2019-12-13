package com.ygoular.notes.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.ygoular.notes.database.dao.NoteDao
import com.ygoular.notes.database.entity.NoteEntity

class NoteRepository(private val mNoteDao: NoteDao) {

    private val mAllNotes: LiveData<List<NoteEntity>> = mNoteDao.fetchAll()
    private val mAllNotesByDate: LiveData<List<NoteEntity>> = mNoteDao.fetchAllByDate()

    fun insert(note: NoteEntity) {
        InsertNoteAsyncTask(mNoteDao).execute(note)
    }

    fun update(note: NoteEntity) {
        UpdateNoteAsyncTask(mNoteDao).execute(note)
    }

    fun delete(note: NoteEntity) {
        DeleteNoteAsyncTask(mNoteDao).execute(note)
    }

    fun deleteAll() {
        DeleteAllNotesAsyncTask(mNoteDao).execute()
    }

    fun fetchAll(): LiveData<List<NoteEntity>> = mAllNotes

    fun fetchAllByDate(): LiveData<List<NoteEntity>> = mAllNotesByDate

    private class InsertNoteAsyncTask(val mNoteDao: NoteDao) : AsyncTask<NoteEntity, Unit, Unit>() {
        override fun doInBackground(vararg notes: NoteEntity) {
            mNoteDao.insert(notes[0])
        }
    }

    private class UpdateNoteAsyncTask(val mNoteDao: NoteDao) : AsyncTask<NoteEntity, Unit, Unit>() {
        override fun doInBackground(vararg notes: NoteEntity) {
            mNoteDao.update(notes[0])
        }
    }

    private class DeleteNoteAsyncTask(val mNoteDao: NoteDao) : AsyncTask<NoteEntity, Unit, Unit>() {
        override fun doInBackground(vararg notes: NoteEntity) {
            mNoteDao.delete(notes[0])
        }
    }

    private class DeleteAllNotesAsyncTask(val mNoteDao: NoteDao) : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg p0: Unit) {
            mNoteDao.deleteAll()
        }
    }
}