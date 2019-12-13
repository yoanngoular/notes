package com.ygoular.notes.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ygoular.notes.R
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.model.Reminder
import kotlinx.android.synthetic.main.note_item.view.*

class NoteAdapter : ListAdapter<NoteEntity, NoteAdapter.NoteHolder>(DIFF_CALLBACK) {

    private var mListener: OnItemClickListener? = null

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        with(getItem(position)) {
            holder.mTextViewTitle.text = mTitle
            holder.mTextViewDescription.text = mDescription
            holder.mTextViewPriority.text = mPriority.toString()
            val nextReminder = getNoteAt(position).getNextReminder()
            if (nextReminder != null) {
                holder.mReminderInfoLayout.visibility = View.VISIBLE
                holder.mReminderInfo.text = Reminder.formatDate(nextReminder)
            } else {
                holder.mReminderInfoLayout.visibility = View.GONE
            }
        }
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
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(note: NoteEntity) = Unit
    }

    fun setOnItemClickListener(listener: (note: NoteEntity) -> Unit = {}) {
        mListener = object : OnItemClickListener {
            override fun onItemClicked(note: NoteEntity) {
                listener(note)
            }
        }
    }
}