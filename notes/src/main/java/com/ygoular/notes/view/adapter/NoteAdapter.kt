package com.ygoular.notes.view.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ygoular.notes.R
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.model.Reminder
import kotlinx.android.synthetic.main.note_item.view.*

class NoteAdapter : ListAdapter<NoteEntity, NoteAdapter.NoteHolder>(DIFF_CALLBACK) {

    private var mListener: OnItemClickListener? = null
    var mSelectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NoteEntity>() {
            override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem.mId == newItem.mId
            }

            override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem.mTitle == newItem.mTitle &&
                        oldItem.mDescription == newItem.mDescription &&
                        oldItem.mPriority == newItem.mPriority &&
                        oldItem.mReminders == newItem.mReminders
            }
        }
    }

    override fun getItemId(position: Int): Long = getItem(position).mId ?: 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        mSelectionTracker?.let { holder.bind(getItem(position), it.isSelected(getItem(position).mId)) }
    }

    fun getNoteAt(position: Int): NoteEntity = getItem(position)

    inner class NoteHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        var mTextViewTitle: TextView = mView.text_view_title
        var mTextViewDescription: TextView = mView.text_view_description
        var mTextViewPriority: TextView = mView.text_view_priority
        var mReminderInfoLayout: RelativeLayout = mView.layout_reminder_info
        var mReminderInfo: TextView = mView.text_view_reminder_info

        init {
            mView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mListener?.onItemClicked(getItem(adapterPosition))
                }
            }

            mView.setOnLongClickListener {
                // Prevent click trigger when long click for item selection mode
                true
            }
        }

        fun bind(note: NoteEntity, isSelected: Boolean = false) {
            with(note) {
                mTextViewTitle.text = mTitle
                mTextViewDescription.text = mDescription
                mTextViewPriority.text = mPriority.toString()
                val nextReminder = note.getNextReminder()
                if (nextReminder != null) {
                    mReminderInfoLayout.visibility = View.VISIBLE
                    mReminderInfo.text = Reminder.formatDate(nextReminder)
                } else {
                    mReminderInfoLayout.visibility = View.GONE
                }
            }
            itemView.isSelected = isSelected
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }

    interface OnItemClickListener {
        fun onItemClicked(note: NoteEntity) = Unit
    }

    fun setOnItemClickListener(
        itemClickListener: (note: NoteEntity) -> Unit = {}
    ) {
        mListener = object : OnItemClickListener {
            override fun onItemClicked(note: NoteEntity) {
                itemClickListener(note)
            }
        }
    }
}

class NoteItemDetailsLookup(private val mRecyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = mRecyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (mRecyclerView.getChildViewHolder(view) as NoteAdapter.NoteHolder)
                .getItemDetails()
        }
        return null
    }
}

class NoteItemKeyProvider(private val mRecyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return mRecyclerView.adapter?.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = mRecyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}