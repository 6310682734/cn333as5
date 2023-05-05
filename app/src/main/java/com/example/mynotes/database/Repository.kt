package com.example.mynotes.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mynotes.domain.model.ColorModel
import com.example.mynotes.domain.model.NoteModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Repository(
    private val noteDao: NoteDao,
    private val colorDao: ColorDao,
    private val dbMapper: DbMapper
) {

    // Working Notes
    private val notesNotInTrashLiveData: MutableLiveData<List<NoteModel>> by lazy {
        MutableLiveData<List<NoteModel>>()
    }

    fun getAllNotesNotInTrash(): LiveData<List<NoteModel>> = notesNotInTrashLiveData

    // Deleted Notes
    private val notesInTrashLiveData: MutableLiveData<List<NoteModel>> by lazy {
        MutableLiveData<List<NoteModel>>()
    }

    fun getAllNotesInTrash(): LiveData<List<NoteModel>> = notesInTrashLiveData

    init {
        initDatabase(this::updateNotesLiveData)
    }

    /**
     * Populates database with colors if it is empty.
     */
    private fun initDatabase(postInitAction: () -> Unit) {
        GlobalScope.launch {
            // Prepopulate colors
            val colors = ColorDbModel.DEFAULT_COLORS.toTypedArray()
            val dbColors = colorDao.getAllSync()
            if (dbColors.isNullOrEmpty()) {
                colorDao.insertAll(*colors)
            }

            // Prepopulate notes
            val notes = NoteDbModel.DEFAULT_NOTES.toTypedArray()
            val dbNotes = noteDao.getAllSync()
            if (dbNotes.isNullOrEmpty()) {
                noteDao.insertAll(*notes)
            }

            postInitAction.invoke()
        }
    }

    // get list of working notes or deleted notes
    private fun getAllNotesDependingOnTrashStateSync(inTrash: Boolean): List<NoteModel> {
        val colorDbModels: Map<Long, ColorDbModel> = colorDao.getAllSync().map { it.id to it }.toMap()
        val dbNotes: List<NoteDbModel> =
            noteDao.getAllSync().filter { it.isInTrash == inTrash }
        return dbMapper.mapNotes(dbNotes, colorDbModels)
    }

    fun insertNote(note: NoteModel) {
        noteDao.insert(dbMapper.mapDbNote(note))
        updateNotesLiveData()
    }

    fun deleteNotes(noteIds: List<Long>) {
        noteDao.delete(noteIds)
        updateNotesLiveData()
    }

    fun moveNoteToTrash(noteId: Long) {
        val dbNote = noteDao.findByIdSync(noteId)
        val newDbNote = dbNote.copy(isInTrash = true)
        noteDao.insert(newDbNote)
        updateNotesLiveData()
    }

    fun restoreNotesFromTrash(noteIds: List<Long>) {
        val dbNotesInTrash = noteDao.getNotesByIdsSync(noteIds)
        dbNotesInTrash.forEach {
            val newDbNote = it.copy(isInTrash = false)
            noteDao.insert(newDbNote)
        }
        updateNotesLiveData()
    }

    fun getAllColors(): LiveData<List<ColorModel>> =
        Transformations.map(colorDao.getAll()) { dbMapper.mapColors(it) }

    private fun updateNotesLiveData() {
        notesNotInTrashLiveData.postValue(getAllNotesDependingOnTrashStateSync(false))
        notesInTrashLiveData.postValue(getAllNotesDependingOnTrashStateSync(true))
    }
}