package com.example.emotionsai.ui.employee.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.RequestMessageDto
import com.example.emotionsai.databinding.ItemRequestMessageBinding

class RequestMessagesAdapter(
    private val onFileClick: (String) -> Unit
) : ListAdapter<RequestMessageDto, RecyclerView.ViewHolder>(Diff) {

    companion object {
        private const val VT_MINE = 1
        private const val VT_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).is_mine) VT_MINE else VT_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_MINE -> MineVH(ItemRequestMessageBinding.inflate(inflater, parent, false), onFileClick)
            else -> OtherVH(ItemRequestMessageBinding.inflate(inflater, parent, false), onFileClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MineVH -> holder.bind(item)
            is OtherVH -> holder.bind(item)
        }
    }

    class MineVH(
        private val vb: ItemRequestMessageBinding,
        private val onFileClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(m: RequestMessageDto) {
            vb.tvSender.text = "You"
            vb.tvTime.text = m.created_at

            vb.tvText.isVisible = !m.text.isNullOrBlank()
            vb.tvText.text = m.text ?: ""

            vb.tvFile.isVisible = !m.file.isNullOrBlank()
            vb.tvFile.setOnClickListener {
                val url = m.file ?: return@setOnClickListener
                onFileClick(url)
            }
        }
    }

    class OtherVH(
        private val vb: ItemRequestMessageBinding,
        private val onFileClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(m: RequestMessageDto) {
            vb.tvSender.text = m.sender_name
            vb.tvTime.text = m.created_at

            vb.tvText.isVisible = !m.text.isNullOrBlank()
            vb.tvText.text = m.text ?: ""

            vb.tvFile.isVisible = !m.file.isNullOrBlank()
            vb.tvFile.setOnClickListener {
                val url = m.file ?: return@setOnClickListener
                onFileClick(url)
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<RequestMessageDto>() {
        override fun areItemsTheSame(oldItem: RequestMessageDto, newItem: RequestMessageDto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RequestMessageDto, newItem: RequestMessageDto) =
            oldItem == newItem
    }
}
