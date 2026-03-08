package com.example.emotionsai.ui.hr.events

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.databinding.ItemHrEventBinding
import com.example.emotionsai.util.formatServerDateTime

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
        h.vb.tvCompany.text = e.company_name
        h.vb.tvDates.text = "${startDate} — ${endDate ?: "no end"}"
        h.vb.tvParticipants.text = "${e.participants_count} participants"

        val ctx = h.itemView.context
        
        // Update Activeness UI
        if (active) {
            h.vb.badgeActive.text = "ACTIVE"
            h.vb.badgeActive.setTextColor(ContextCompat.getColor(ctx, R.color.primary))
            h.vb.badgeActive.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.bottom_nav_indicator))
            h.vb.cardView.strokeColor = ContextCompat.getColor(ctx, R.color.primary)
            h.vb.cardView.alpha = 1.0f
        } else {
            h.vb.badgeActive.text = "PAST"
            h.vb.badgeActive.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            h.vb.badgeActive.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.stroke_soft))
            h.vb.cardView.strokeColor = ContextCompat.getColor(ctx, R.color.stroke_soft)
            h.vb.cardView.alpha = 0.7f
        }

        // Buttons state
        h.vb.btnEdit.isEnabled = active
        h.vb.btnDelete.isEnabled = active
        
        // Click Listeners
        h.vb.btnEdit.setOnClickListener { onEdit(e) }
        h.vb.btnDelete.setOnClickListener { onDelete(e) }
    }
}
