package com.example.emotionsai.ui.hr.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.databinding.ItemHrEventBinding
import java.time.OffsetDateTime

class HrEventsAdapter(
    private val isActive: (HrEventDto) -> Boolean,
    private val onEdit: (HrEventDto) -> Unit,
    private val onDelete: (HrEventDto) -> Unit
) : ListAdapter<HrEventDto, HrEventsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<HrEventDto>() {
        override fun areItemsTheSame(o: HrEventDto, n: HrEventDto) = o.id == n.id
        override fun areContentsTheSame(o: HrEventDto, n: HrEventDto) = o == n
    }

    inner class VH(val vb: ItemHrEventBinding) : RecyclerView.ViewHolder(vb.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemHrEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val e = getItem(position)
        val active = isActive(e)

        h.vb.tvTitle.text = e.title
        h.vb.tvDates.text = "${e.starts_at} — ${e.ends_at ?: "no end"}"
        h.vb.tvParticipants.text = "Participants: ${e.participants_count}"

        h.vb.badgeActive.visibility = if (active) View.VISIBLE else View.GONE

        h.vb.btnEdit.isEnabled = active
        h.vb.btnDelete.isEnabled = active

        h.vb.btnEdit.setOnClickListener { onEdit(e) }
        h.vb.btnDelete.setOnClickListener { onDelete(e) }
    }
}
