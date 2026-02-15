package com.example.emotionsai.ui.hr.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.databinding.ItemHrEventBinding
import com.example.emotionsai.util.formatServerDateTime
import androidx.core.content.ContextCompat

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

        val startDate = formatServerDateTime(e.starts_at)
        val endDate = formatServerDateTime(e.ends_at)

        h.vb.tvTitle.text = e.title
        h.vb.tvDates.text = "${startDate} — ${endDate ?: "no end"}"
        h.vb.tvParticipants.text = "Participants: ${e.participants_count}"
        h.vb.badgeActive.text = if (active) "ACTIVE" else "PAST"

        val ctx = h.itemView.context

        val enabledEditColor = ContextCompat.getColor(ctx, R.color.primary)
        val enabledDeleteColor = ContextCompat.getColor(ctx, R.color.error)
        val disabledColor = ContextCompat.getColor(ctx, R.color.text_secondary) // серый

        // --- Edit ---
        h.vb.btnEdit.isEnabled = active
        h.vb.btnEdit.setTextColor(if (active) enabledEditColor else disabledColor)
        h.vb.btnEdit.strokeColor = android.content.res.ColorStateList.valueOf(
            if (active) enabledEditColor else disabledColor
        )

        // --- Delete ---
        h.vb.btnDelete.isEnabled = active
        h.vb.btnDelete.setTextColor(if (active) enabledDeleteColor else disabledColor)
        h.vb.btnDelete.strokeColor = android.content.res.ColorStateList.valueOf(
            if (active) enabledDeleteColor else disabledColor
        )

        h.vb.btnEdit.setOnClickListener { if (active) onEdit(e) }
        h.vb.btnDelete.setOnClickListener { if (active) onDelete(e) }
    }

}
