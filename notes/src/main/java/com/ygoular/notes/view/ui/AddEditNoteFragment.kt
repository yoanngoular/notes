package com.ygoular.notes.view.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ygoular.notes.R
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.database.entity.NoteEntity.Companion.MAX_NUMBER_OF_REMINDERS
import com.ygoular.notes.database.entity.Priority
import com.ygoular.notes.model.Reminder
import com.ygoular.notes.view.ui.NoteActivity.Companion.mSupportActionBar
import com.ygoular.notes.viewmodel.NoteViewModel
import com.google.gson.Gson
import com.ygoular.notes.BaseApplication
import com.ygoular.notes.view.adapter.ReminderAdapter
import kotlinx.android.synthetic.main.fragment_add_edit_note.*
import java.util.Calendar

class AddEditNoteFragment : Fragment() {

    private val mViewModel: NoteViewModel by viewModels()
    private val mReminderAdapter = ReminderAdapter()

    private lateinit var mNavController: NavController
    private lateinit var mSaveMode: SaveMode
    private lateinit var mNotePriority: Priority

    private var mNoteId: Long = NoteEntity.WRONG_ID
    private var mNoteTitle: String = ""
    private var mNoteDescription: String = ""
    private var mNewReminderList = mutableSetOf<Reminder>()
    private var mOldReminderList = mutableSetOf<Reminder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mSupportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNavController = Navigation.findNavController(view)

        number_picker_priority.minValue = Priority.MIN.value
        number_picker_priority.maxValue = Priority.MAX.value

        fetchBundleData()

        initRecyclerView()

        button_save.setOnClickListener {

            val title = edit_text_title.text.toString()
            val description = edit_text_description.text.toString()

            if (title.isEmpty()) {
                val errorMessage: String = getString(R.string.error_title_empty)
                edit_text_title.error = errorMessage
                return@setOnClickListener
            }

            (activity as NoteActivity).hideKeyboard(view)

            val newNote = NoteEntity(
                title,
                description,
                Priority.fromValue(number_picker_priority.value),
                mNewReminderList
            )

            when (mSaveMode) {
                SaveMode.CREATE -> mViewModel.insert(newNote)
                SaveMode.UPDATE -> {
                    if (NoteEntity.WRONG_ID != mNoteId && ::mNotePriority.isInitialized) {
                        newNote.mId = mNoteId
                        val oldNote = NoteEntity(
                            mNoteTitle,
                            mNoteDescription,
                            mNotePriority,
                            mOldReminderList
                        )
                        if (!newNote.isEquivalent(oldNote)) {
                            mViewModel.update(newNote)
                        }
                    } else {
                        view.snack(getString(R.string.update_note_failed))
                    }
                }
            }

            Navigation.findNavController(view).popBackStack()
        }

        edit_text_title.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_note_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> mNavController.popBackStack()
            R.id.add_reminder -> {
                if (mNewReminderList.size >= MAX_NUMBER_OF_REMINDERS) {
                    layout_add_edit_note.snack(
                        "Can't add more than $MAX_NUMBER_OF_REMINDERS reminders per note."
                    )
                } else {
                    addEditReminder(Calendar.getInstance())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        recycler_view_reminders.layoutManager = LinearLayoutManager(context)
        mReminderAdapter.setOnEditClickedListener { reminder ->
            val calendar = Calendar.getInstance()
            calendar.time = reminder.mDate
            addEditReminder(calendar, reminder.mId)
        }
        mReminderAdapter.setOnDeleteClickedListener { reminder ->
            mNewReminderList.remove(reminder)
            submitReminderList()
        }
        recycler_view_reminders.adapter = mReminderAdapter
    }

    private fun fetchBundleData() {
        arguments?.apply {
            mSaveMode = get(getString(R.string.bundle_save_mode)) as SaveMode
            mNoteId = getLong(getString(R.string.bundle_note_id))
            button_save.text = mSaveMode.value

            if (SaveMode.UPDATE == mSaveMode) {
                activity?.setTitle(R.string.title_update_note)
                mNoteTitle = getString(getString(R.string.bundle_note_title)).toString()
                mNoteDescription = getString(getString(R.string.bundle_note_description)).toString()
                mNotePriority = get(getString(R.string.bundle_note_priority)) as Priority

                edit_text_title.setText(mNoteTitle)
                edit_text_description.setText(mNoteDescription)
                number_picker_priority.value = mNotePriority.value

                mNewReminderList = getStringArray(getString(R.string.bundle_reminders))!!.map {
                    Gson().fromJson(it, Reminder::class.java)
                }.toMutableSet()
                mOldReminderList.addAll(mNewReminderList)
                submitReminderList()
            } else {
                activity?.setTitle(R.string.title_create_note)
            }
        }
    }

    private fun addEditReminder(startDate: Calendar, id: Int = 0) {
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val currentTime = Calendar.getInstance()
                val reminderTime = Calendar.getInstance()
                reminderTime.set(year, month, dayOfMonth)

                if (reminderTime.time >= currentTime.time) {
                    val timePickerDialog: TimePickerDialog? = context?.let {
                        TimePickerDialog(
                            it,
                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                reminderTime.set(year, month, dayOfMonth, hourOfDay, minute, 0)

                                if (reminderTime.time.after(Calendar.getInstance().time)) {

                                    if (id != Reminder.INVALID_ID) { // Editing reminder
                                        val updatedReminder = Reminder(id, reminderTime.time)
                                        if (mNewReminderList.contains(updatedReminder)) {
                                            reminderAlreadyExists()
                                        } else {
                                            val reminderIndex = mNewReminderList.indexOf(
                                                Reminder(id, startDate.time)
                                            )
                                            val temporaryArray = mNewReminderList.toTypedArray()
                                            temporaryArray[reminderIndex] = updatedReminder
                                            mNewReminderList = temporaryArray.toMutableSet()
                                            submitReminderList()
                                        }
                                    } else { // Creating reminder
                                        val reminder = Reminder(
                                            createReminderId(currentTime), reminderTime.time
                                        )
                                        if (mNewReminderList.add(reminder)) submitReminderList()
                                        else reminderAlreadyExists()
                                    }
                                } else {
                                    layout_add_edit_note.snack(
                                        "The reminder must be set after the current time."
                                    )
                                }
                            },
                            startDate.get(Calendar.HOUR_OF_DAY),
                            startDate.get(Calendar.MINUTE), true
                        )
                    }
                    timePickerDialog?.show()
                } else {
                    layout_add_edit_note.snack("The reminder can't be set in the past.")
                }
            }

        val datePickerDialog: DatePickerDialog? = context?.let {
            DatePickerDialog(
                it, dateSetListener,
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
            )
        }

        datePickerDialog?.show()
    }

    private fun createReminderId(currentTime: Calendar): Int =
        (currentTime.timeInMillis / Reminder.TIME_IN_SECONDS).toInt()

    private fun reminderAlreadyExists() {
        layout_add_edit_note.snack("Can't set an reminder that already exists.")
    }

    private fun submitReminderList() {
        mReminderAdapter.submitList(mNewReminderList.toMutableList().sortedBy { it.mDate.time })
    }

    enum class SaveMode(val value: String) {
        CREATE(BaseApplication.mApplicationComponent.application().getString(R.string.create)),
        UPDATE(BaseApplication.mApplicationComponent.application().getString(R.string.update))
    }
}
