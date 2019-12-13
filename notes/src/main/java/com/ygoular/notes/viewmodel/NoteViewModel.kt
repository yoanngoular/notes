package com.ygoular.notes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.ygoular.notes.BaseApplication
import com.ygoular.notes.model.SortingStrategy
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.helper.PrefsHelper
import com.ygoular.notes.repository.NoteRepository

class NoteViewModel : ViewModel() {
    private val mNoteRepository: NoteRepository =
        BaseApplication.mApplicationComponent.noteRepository()
    private val mAllNotes: LiveData<List<NoteEntity>> = mNoteRepository.fetchAll()
    private val mAllNotesByDate: LiveData<List<NoteEntity>> = mNoteRepository.fetchAllByDate()
    private val mAllNotesMediator = MediatorLiveData<List<NoteEntity>>()

    init {
        mAllNotesMediator.addSource(mAllNotes) { value ->
            if (SortingStrategy.fetchSortingStrategy() == SortingStrategy.PRIORITY) {
                mAllNotesMediator.value = value
            }
        }
        mAllNotesMediator.addSource(mAllNotesByDate) { value ->
            if (SortingStrategy.fetchSortingStrategy() == SortingStrategy.MODIFICATION) {
                mAllNotesMediator.value = value
            }
        }
    }

    fun reorderNotes(sortingStrategy: SortingStrategy) {
        if (sortingStrategy == SortingStrategy.fetchSortingStrategy()) {
            return
        }

        PrefsHelper[PrefsHelper.PREF_SORTING_STRATEGY] = sortingStrategy.value

        when (sortingStrategy) {
            SortingStrategy.PRIORITY -> mAllNotesMediator.setValue(mAllNotes.value)
            SortingStrategy.MODIFICATION -> mAllNotesMediator.setValue(mAllNotesByDate.value)
        }
    }

    fun insert(note: NoteEntity) {
        note.setUpReminders()
        return mNoteRepository.insert(note)
    }

    fun update(note: NoteEntity) {
        note.cancelReminders()
        note.setUpReminders()
        return mNoteRepository.update(note)
    }

    fun delete(note: NoteEntity) {
        note.cancelReminders()
        return mNoteRepository.delete(note)
    }

    fun deleteAll() {
        fetchAll().value?.forEach { it.cancelReminders() }
        return mNoteRepository.deleteAll()
    }

    fun fetchAll(): LiveData<List<NoteEntity>> = mAllNotesMediator
}