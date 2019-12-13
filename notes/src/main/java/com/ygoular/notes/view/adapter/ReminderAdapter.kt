package com.ygoular.notes.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ygoular.notes.R
import com.ygoular.notes.model.Reminder
import kotlinx.android.synthetic.main.reminder_item.view.*

class ReminderAdapter : ListAdapter<Reminder, ReminderAdapter.ReminderHolder>(DIFF_CALLBACK) {

    private var mEditListener: OnItemClickListener? = null
    private var mDeleteListener: OnItemClickListener? = null

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(
                oldItem: Reminder,
                newItem: Reminder
            ): Boolean =
                oldItem.mId == newItem.mId

            override fun areContentsTheSame(
                oldItem: Reminder,
                newItem: Reminder
            ): Boolean {
                return oldItem.mDate.time == newItem.mDate.time
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderHolder {
        return ReminderHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.reminder_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReminderHolder, position: Int) {
        with(getItem(position)) {
            holder.mTextViewReminder.text = Reminder.formatDate(this.mDate)
        }
    }

    inner class ReminderHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        var mTextViewReminder: TextView = mView.text_view_reminder

        init {
            mView.image_button_edit_reminder.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mEditListener?.onEditClicked(getItem(adapterPosition))
                }
            }
            mView.image_button_delete_reminder.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mDeleteListener?.onDeleteClicked(getItem(adapterPosition))
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onEditClicked(reminder: Reminder) = Unit
        fun onDeleteClicked(reminder: Reminder) = Unit
    }

    fun setOnEditClickedListener(listener: (reminder: Reminder) -> Unit = {}) {
        mEditListener = object : OnItemClickListener {
            override fun onEditClicked(reminder: Reminder) {
                listener(reminder)
            }
        }
    }

    fun setOnDeleteClickedListener(listener: (reminder: Reminder) -> Unit = {}) {
        mDeleteListener = object : OnItemClickListener {
            override fun onDeleteClicked(reminder: Reminder) {
                listener(reminder)
            }
        }
    }
}