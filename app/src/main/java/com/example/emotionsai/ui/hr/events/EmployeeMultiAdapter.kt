package com.example.emotionsai.ui.hr.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionsai.data.remote.EmployeeDto
import android.widget.CheckBox
import android.widget.TextView
import com.example.emotionsai.R

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

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val checkbox = v.findViewById<CheckBox>(R.id.chkEmployee)
        val name = v.findViewById<TextView>(R.id.tvName)
        val dept = v.findViewById<TextView>(R.id.tvDept)
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
            if (isChecked) checked.add(e.id)
            else checked.remove(e.id)

            onCheckedChange(e.id, isChecked)
        }
    }
}
