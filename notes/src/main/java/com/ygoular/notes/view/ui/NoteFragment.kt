package com.ygoular.notes.view.ui

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ygoular.notes.R
import com.ygoular.notes.model.SortingStrategy
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.database.entity.Priority
import com.ygoular.notes.view.ui.NoteActivity.Companion.mSupportActionBar
import com.ygoular.notes.viewmodel.NoteViewModel
import com.google.gson.Gson
import com.ygoular.notes.model.Reminder
import com.ygoular.notes.view.adapter.NoteItemDetailsLookup
import com.ygoular.notes.view.adapter.NoteAdapter
import com.ygoular.notes.view.adapter.NoteItemKeyProvider
import kotlinx.android.synthetic.main.fragment_note.*
import java.lang.StringBuilder

class NoteFragment : Fragment() {

    companion object {
        private const val SAVED_SELECTION_KEY = "selection"
    }

    private val mViewModel: NoteViewModel by viewModels()
    private val mNoteAdapter = NoteAdapter()

    private val mDeleteBackground: Drawable? by lazy {
        context?.let { ContextCompat.getDrawable(it, R.drawable.bg_rectangle_radius_small) }
    }
    private val mDeleteIcon: Drawable? by lazy {
        context?.let { ContextCompat.getDrawable(it, R.drawable.ic_delete_sweep) }
    }

    private var mCurrentNotes: List<NoteEntity> = listOf()

    private lateinit var mSelectionTracker: SelectionTracker<Long>
    private lateinit var mMenu: Menu
    private lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle(R.string.title_list_of_notes)
        mSupportActionBar?.setDisplayHomeAsUpEnabled(false)

        mNavController = Navigation.findNavController(view)

        recycler_view_notes.layoutManager = LinearLayoutManager(context)
        recycler_view_notes.setHasFixedSize(true)

        recycler_view_notes.adapter = mNoteAdapter

        mSelectionTracker = SelectionTracker.Builder<Long>(
            "noteSelection",
            recycler_view_notes,
            NoteItemKeyProvider(recycler_view_notes),
            NoteItemDetailsLookup(recycler_view_notes),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        mSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (::mMenu.isInitialized) {
                    updateMenuVisibility()
                }
            }
        })
        mNoteAdapter.mSelectionTracker = mSelectionTracker

        mNoteAdapter.setOnItemClickListener { note ->
            val bundle = bundleOf(
                getString(R.string.bundle_note_id) to note.mId,
                getString(R.string.bundle_note_title) to note.mTitle,
                getString(R.string.bundle_note_description) to note.mDescription,
                getString(R.string.bundle_note_priority) to Priority.fromValue(note.mPriority),
                getString(R.string.bundle_save_mode) to AddEditNoteFragment.SaveMode.UPDATE,
                getString(R.string.bundle_reminders)
                        to note.mReminders.map { Gson().toJson(it) }.toTypedArray()
            )
            mNavController.navigate(R.id.action_noteFragment_to_addEditNoteFragment, bundle)
        }

        mViewModel.fetchAll().observe(
            viewLifecycleOwner, Observer<List<NoteEntity>> { notes: List<NoteEntity> ->
                mCurrentNotes = notes
                if (notes.isEmpty()) {
                    text_view_no_notes.visibility = View.VISIBLE
                } else {
                    text_view_no_notes.visibility = View.GONE
                }
                mNoteAdapter.submitList(notes)
            })

        button_add_note.setOnClickListener {
            (activity as NoteActivity).navigateCreateNote(mNavController)
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.START or ItemTouchHelper.END
        ) {

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val backgroundCornerOffset = 0

                val itemView = viewHolder.itemView

                val icon: Drawable? = mDeleteIcon

                when {
                    dX > 0 -> { // Swiping to the right
                        if (icon != null) {
                            val margin = (itemView.height - icon.intrinsicHeight) / 2
                            mDeleteIcon?.setBounds(
                                itemView.left + margin,
                                itemView.top + margin,
                                itemView.left + margin + icon.intrinsicWidth,
                                itemView.top + margin + icon.intrinsicHeight
                            )
                        }

                        mDeleteBackground?.setBounds(
                            itemView.left, itemView.top,
                            itemView.left + dX.toInt() + backgroundCornerOffset,
                            itemView.bottom
                        )
                    }
                    dX < 0 -> { // Swiping to the left
                        if (icon != null) {
                            val margin = (itemView.height - icon.intrinsicHeight) / 2
                            mDeleteIcon?.setBounds(
                                itemView.right - margin - icon.intrinsicWidth,
                                itemView.top + margin,
                                itemView.right - margin,
                                itemView.top + margin + icon.intrinsicHeight
                            )
                        }

                        mDeleteBackground?.setBounds(
                            itemView.right + dX.toInt() - backgroundCornerOffset,
                            itemView.top, itemView.right, itemView.bottom
                        )
                    }
                    else -> { // View is unswiped
                        mDeleteBackground?.setBounds(0, 0, 0, 0)
                    }
                }

                mDeleteBackground?.draw(c)
                mDeleteIcon?.draw(c)

                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState,
                    isCurrentlyActive
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val noteToDelete = mNoteAdapter.getNoteAt(viewHolder.adapterPosition)
                mViewModel.delete(noteToDelete)
                view.snack(getString(R.string.delete_note_successful), getString(R.string.undo),
                    View.OnClickListener { mViewModel.insert(noteToDelete) })
            }
        }).attachToRecyclerView(recycler_view_notes)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLongArray(
            SAVED_SELECTION_KEY,
            mSelectionTracker.selection.toList().toLongArray()
        )
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        savedInstanceState?.getLongArray(SAVED_SELECTION_KEY)?.forEach {
            mSelectionTracker.select(it)
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        (activity as NoteActivity).hideKeyboard(view)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mMenu = menu
        inflater.inflate(R.menu.note_menu, menu)
        updateMenuVisibility()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all -> {
                if (text_view_no_notes.visibility == View.VISIBLE) {
                    view?.snack(getString(R.string.no_notes_to_delete))
                } else {
                    val dialog: AlertDialog? = context?.let {
                        AlertDialog.Builder(it)
                            .setTitle(getString(R.string.delete_all_notes))
                            .setMessage(getString(R.string.delete_all_notes_confirmation))
                            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                mViewModel.deleteAll()
                                view?.snack(
                                    getString(R.string.delete_all_notes_successful),
                                    indefinite = true
                                )
                            }
                            .setNegativeButton(getString(R.string.abort)) { dialog_, _ -> dialog_.dismiss() }
                            .create()
                    }
                    dialog?.show()
                }
            }
            R.id.sort_notes -> {
                val listItems = SortingStrategy.values().map { it.value }.toTypedArray()
                var selectedStrategy: Int = SortingStrategy.fetchSortingStrategy().ordinal

                val builder = context?.let {
                    AlertDialog.Builder(it)
                        .setTitle(getString(R.string.sort_by))
                        .setSingleChoiceItems(listItems, selectedStrategy) { _, which ->
                            selectedStrategy = which
                        }
                        .setPositiveButton(getString(R.string.done)) { dialog, _ ->
                            dialog.dismiss()
                            mViewModel.reorderNotes(SortingStrategy.fromValue(listItems[selectedStrategy]))
                        }
                        .create()
                }

                builder?.show()
            }
            R.id.share_notes -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, getString(R.string.sharing_title))
                    putExtra(Intent.EXTRA_TEXT, formatNoteSelection())
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            android.R.id.home -> {
                mSelectionTracker.clearSelection()
                updateMenuVisibility()
            }
            // TODO: Add Export note feature (create custom file to be re imported)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateMenuVisibility() {
        if (mSelectionTracker.selection.size() > 0) {
            mSupportActionBar?.setDisplayHomeAsUpEnabled(true)
            mMenu.setGroupVisible(R.id.selection_mode_menu, true)
            mMenu.setGroupVisible(R.id.main_menu, false)
        } else {
            mSupportActionBar?.setDisplayHomeAsUpEnabled(false)
            mMenu.setGroupVisible(R.id.selection_mode_menu, false)
            mMenu.setGroupVisible(R.id.main_menu, true)
        }
    }

    private fun formatNoteSelection(): String {
        val formattedMessage =
            StringBuilder(getString(R.string.sharing_intro)).appendln().appendln()
        for (selectedItem in mSelectionTracker.selection) {
            val selectedNote = mCurrentNotes.find { it.mId == selectedItem }
            if (selectedNote != null) {
                with(selectedNote) {
                    formattedMessage.append("[$mTitle]").appendln().appendln()
                    if (mDescription.isNotEmpty()) {
                        formattedMessage.append(mDescription).appendln().appendln()
                    }
                    if (mReminders.isNotEmpty()) {
                        mReminders.forEach {
                            formattedMessage.append(
                                getString(
                                    R.string.reminder_set,
                                    Reminder.formatDate(it.mDate)
                                )
                            ).appendln().appendln()
                        }
                    }
                }
            }
        }
        return formattedMessage.toString()
    }
}
