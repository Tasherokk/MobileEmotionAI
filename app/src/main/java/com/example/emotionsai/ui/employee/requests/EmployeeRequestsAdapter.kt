package com.example.emotionsai.ui.employee.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.EmployeeRequestItemDto
import com.example.emotionsai.databinding.ItemEmployeeRequestBinding
import com.example.emotionsai.util.formatServerDateTime

class EmployeeRequestsAdapter(
    private val onClick: (EmployeeRequestItemDto) -> Unit
) : ListAdapter<EmployeeRequestItemDto, EmployeeRequestsAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemEmployeeRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(vb, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val vb: ItemEmployeeRequestBinding,
        private val onClick: (EmployeeRequestItemDto) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(item: EmployeeRequestItemDto) {
            vb.tvTitle.text = item.type_name
            vb.tvStatus.text = item.status
            vb.tvDescription.text = "HR: ${item.hr_name} (@${item.hr_username})"
            vb.tvDate.text = formatServerDateTime(item.created_at) ?: ""
            vb.tvMessagesCount.text = "${item.messages_count} messages"
            vb.tvLastMessage.text = if (item.last_message_at != null) {
                "Updated: ${formatServerDateTime(item.last_message_at)}"
            } else {
                "No messages"
            }

            vb.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<EmployeeRequestItemDto>() {
        override fun areItemsTheSame(oldItem: EmployeeRequestItemDto, newItem: EmployeeRequestItemDto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EmployeeRequestItemDto, newItem: EmployeeRequestItemDto) =
            oldItem == newItem
    }
}
