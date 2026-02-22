package com.example.emotionsai.ui.employee.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.RequestMessageDto
import com.example.emotionsai.databinding.ItemRequestMessageBinding
import com.example.emotionsai.util.formatServerDateTime
import java.net.URLDecoder

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
        val vb = ItemRequestMessageBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VT_MINE -> MineVH(vb, onFileClick)
            else -> OtherVH(vb, onFileClick)
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
            vb.tvTime.text = formatServerDateTime(m.created_at)

            vb.tvText.isVisible = !m.text.isNullOrBlank()
            vb.tvText.text = m.text.orEmpty()

            bindFile(vb, m, onFileClick)
        }
    }

    class OtherVH(
        private val vb: ItemRequestMessageBinding,
        private val onFileClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(m: RequestMessageDto) {
            vb.tvSender.text = m.sender_name
            vb.tvTime.text = formatServerDateTime(m.created_at)

            vb.tvText.isVisible = !m.text.isNullOrBlank()
            vb.tvText.text = m.text.orEmpty()

            bindFile(vb, m, onFileClick)
        }
    }

    private object Diff : DiffUtil.ItemCallback<RequestMessageDto>() {
        override fun areItemsTheSame(oldItem: RequestMessageDto, newItem: RequestMessageDto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RequestMessageDto, newItem: RequestMessageDto) =
            oldItem == newItem
    }
}

/** Вынесено, чтобы не дублировать код в двух VH */
private fun bindFile(
    vb: ItemRequestMessageBinding,
    m: RequestMessageDto,
    onFileClick: (String) -> Unit
) {
    val url = m.file
    vb.tvFile.isVisible = !url.isNullOrBlank()
    if (url.isNullOrBlank()) return

    // ✅ показываем имя файла вместо "File"
    vb.tvFile.text = fileNameFromUrl(url)

    vb.tvFile.setOnClickListener { onFileClick(url) }
}

/** Берём последний сегмент URL и декодим (%20 -> пробел) */
private fun fileNameFromUrl(url: String): String {
    val noQuery = url.substringBefore("?").substringBefore("#")
    val last = noQuery.substringAfterLast("/", missingDelimiterValue = noQuery)
    val decoded = runCatching { URLDecoder.decode(last, "UTF-8") }.getOrDefault(last)
    return decoded.ifBlank { "attachment" }
}
