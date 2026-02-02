package com.example.emotionsai.ui.employee.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.EmployeeEventDto
import com.example.emotionsai.databinding.ItemEmployeeEventBinding

class EmployeeEventsAdapter(
    private val onClick: (EmployeeEventDto) -> Unit
) : RecyclerView.Adapter<EmployeeEventsAdapter.VH>() {

    private val items = mutableListOf<EmployeeEventDto>()

    fun submit(list: List<EmployeeEventDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vb = ItemEmployeeEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(vb, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(
        private val vb: ItemEmployeeEventBinding,
        private val onClick: (EmployeeEventDto) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(item: EmployeeEventDto) {
            vb.tvTitle.text = item.title

            val end = item.ends_at ?: "—"
            vb.tvDates.text = "${item.starts_at} — $end"

            if (item.has_feedback) {
                vb.tvStatus.text = "Completed"
                vb.root.isEnabled = false
                vb.root.alpha = 0.55f
            } else {
                vb.tvStatus.text = "Open"
                vb.root.isEnabled = true
                vb.root.alpha = 1f
                vb.root.setOnClickListener { onClick(item) }
            }
        }
    }
}
