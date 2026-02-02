package com.example.emotionsai.ui.hr.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.HrRequestItemDto
import com.example.emotionsai.databinding.ItemHrRequestBinding

class HrRequestsAdapter(
    private val onClick: (HrRequestItemDto) -> Unit
) : ListAdapter<HrRequestItemDto, HrRequestsAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemHrRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(vb, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val vb: ItemHrRequestBinding,
        private val onClick: (HrRequestItemDto) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(item: HrRequestItemDto) {
            vb.tvTitle.text = item.type_name
            vb.tvSubtitle.text = "From: ${item.employee_name} (@${item.employee_username})"
            vb.tvStatus.text = item.status
            vb.tvCreatedAt.text = item.created_at
            vb.tvLastMessage.text = item.last_message_at ?: "—"
            vb.tvMessagesCount.text = item.messages_count.toString()

            vb.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<HrRequestItemDto>() {
        override fun areItemsTheSame(oldItem: HrRequestItemDto, newItem: HrRequestItemDto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HrRequestItemDto, newItem: HrRequestItemDto) =
            oldItem == newItem
    }
}
