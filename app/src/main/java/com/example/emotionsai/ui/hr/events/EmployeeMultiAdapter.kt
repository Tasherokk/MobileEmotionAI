package com.example.emotionsai.ui.hr.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.EmployeeDto

class EmployeeMultiAdapter(
    private val onCheckedChange: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<EmployeeMultiAdapter.VH>() {

    private val items = mutableListOf<EmployeeDto>()
    private val checked = mutableSetOf<Int>()

    fun submitList(list: List<EmployeeDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelected(): List<Int> = checked.toList()

    // ✅ NEW
    fun setPreselected(ids: Collection<Int>) {
        checked.clear()
        checked.addAll(ids)
        notifyDataSetChanged()
    }

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val checkbox: CheckBox = v.findViewById(R.id.chkEmployee)
        val name: TextView = v.findViewById(R.id.tvName)
        val dept: TextView = v.findViewById(R.id.tvDept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee_checkbox, parent, false)
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val e = items[pos]

        holder.name.text = e.name
        holder.dept.text = e.department_name ?: ""

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = checked.contains(e.id)

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checked.add(e.id) else checked.remove(e.id)
            onCheckedChange(e.id, isChecked)
        }

        // ✅ UX: клик по строке тоже переключает чекбокс
        holder.v.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }
}
