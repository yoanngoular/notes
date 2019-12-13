package com.ygoular.notes.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.ygoular.notes.database.entity.NoteEntity

/**
 * Room Data Access Object (DAO) for the NoteEntity object
 *
 * Define the different operations involving a NoteEntity
 * that are available on the database.
 *
 * This interface is declared as an abstract function that
 * returns a NoteDao in the database class (AppDatabase).
 *
 */
@Dao
interface NoteDao {

    @Insert(onConflict = REPLACE)
    fun insert(note: NoteEntity)

    @Update
    fun update(note: NoteEntity)

    @Delete
    fun delete(note: NoteEntity)

    @Query("DELETE FROM note_table")
    fun deleteAll()

    @Query("SELECT * FROM note_table ORDER BY priority DESC, date DESC")
    fun fetchAll(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM note_table ORDER BY date DESC")
    fun fetchAllByDate(): LiveData<List<NoteEntity>>
}