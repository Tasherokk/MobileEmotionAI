package com.example.emotionsai.ui.hr.events

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentCreateEventBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.profile.ProfileViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateEventFragment : Fragment(R.layout.fragment_create_event) {

    private var _vb: FragmentCreateEventBinding? = null
    private val vb get() = _vb!!

    private val vm: CreateEventViewModel by viewModels {
        ServiceLocator.createEventVMFactory(requireContext())
    }

    private val profileVm: ProfileViewModel by viewModels()
    private var companyId: Int? = null

    private lateinit var adapter: EmployeeMultiAdapter

    private val args: CreateEventFragmentArgs by navArgs()
    private val isEditMode: Boolean get() = args.eventId != 0

    private var appliedParticipants = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentCreateEventBinding.bind(view)

        adapter = EmployeeMultiAdapter { _, _ -> }
        vb.rvEmployees.layoutManager = LinearLayoutManager(requireContext())
        vb.rvEmployees.adapter = adapter

        setupProfile()
        setupPickers()
        setupObservers()

        vm.loadEmployees()

        if (isEditMode) {
            vb.btnCreate.text = "Save changes"
            vm.loadEventDetails(args.eventId)
        }

        setupCreateBtn()
    }

    private fun setupProfile() {
        profileVm.me.observe(viewLifecycleOwner) { me ->
            if (me != null) {
                companyId = me.company
                vb.tvCompany.text = me.company_name
            }
        }
        profileVm.loadMe()
    }

    private fun setupPickers() {
        vb.inputStart.setOnClickListener {
            val dpStart = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.ThemeOverlay_App_DatePicker)
                .setTitleText("Select Start Date")
                .build()
            dpStart.addOnPositiveButtonClickListener { millis ->
                vb.inputStart.setText(formatDate(millis))
            }
            dpStart.show(parentFragmentManager, "startDate")
        }

        vb.inputEnd.setOnClickListener {
            val dpEnd = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.ThemeOverlay_App_DatePicker)
                .setTitleText("Select End Date")
                .build()
            dpEnd.addOnPositiveButtonClickListener { millis ->
                vb.inputEnd.setText(formatDate(millis))
            }
            dpEnd.show(parentFragmentManager, "endDate")
        }

        vb.inputStartTime.setOnClickListener {
            showTimePicker { hh, mm ->
                vb.inputStartTime.setText(String.format(Locale.US, "%02d:%02d", hh, mm))
            }
        }

        vb.inputEndTime.setOnClickListener {
            showTimePicker { hh, mm ->
                vb.inputEndTime.setText(String.format(Locale.US, "%02d:%02d", hh, mm))
            }
        }
    }

    private fun showTimePicker(onPicked: (hour: Int, minute: Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select time")
            .build()

        picker.addOnPositiveButtonClickListener {
            onPicked(picker.hour, picker.minute)
        }
        picker.show(parentFragmentManager, "timePicker")
    }

    private fun setupCreateBtn() {
        vb.btnCreate.setOnClickListener {
            val title = vb.inputTitle.text?.toString().orEmpty().trim()
            val startDate = vb.inputStart.text?.toString()
            val startTime = vb.inputStartTime.text?.toString()
            val endDate = vb.inputEnd.text?.toString()
            val endTime = vb.inputEndTime.text?.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Enter title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cid = companyId
            if (cid == null) {
                Toast.makeText(requireContext(), "No company", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startIso = buildIsoDateTimeOrNull(startDate, startTime)
            if (startIso == null) {
                Toast.makeText(requireContext(), "Pick start date & time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val endIso = buildIsoDateTimeOrNull(endDate, endTime)
            val parts = adapter.getSelected()

            if (isEditMode) {
                vm.updateEvent(args.eventId, title, startIso, endIso, cid, parts)
            } else {
                vm.createEvent(title, startIso, endIso, cid, parts)
            }
        }
    }

    private fun setupObservers() {
        vm.employees.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }
        vm.eventDetails.observe(viewLifecycleOwner) { details ->
            if (details == null) return@observe
            vb.inputTitle.setText(details.title)
            val (sd, st) = parseServerToLocal(details.starts_at)
            vb.inputStart.setText(sd)
            vb.inputStartTime.setText(st)
            val endStr = details.ends_at
            if (endStr.isNullOrBlank()) {
                vb.inputEnd.setText("")
                vb.inputEndTime.setText("")
            } else {
                val (ed, et) = parseServerToLocal(endStr)
                vb.inputEnd.setText(ed)
                vb.inputEndTime.setText(et)
            }
            if (!appliedParticipants) {
                adapter.setPreselected(details.participants)
                appliedParticipants = true
            }
        }
        vm.loading.observe(viewLifecycleOwner) { vb.progress.visibility = if (it) View.VISIBLE else View.GONE }
        vm.success.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                vm.successHandled()
                Toast.makeText(requireContext(), if (isEditMode) "Event updated!" else "Event created!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
        vm.error.observe(viewLifecycleOwner) { if (!it.isNullOrEmpty()) Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
    }

    private fun buildIsoDateTimeOrNull(date: String?, time: String?): String? {
        val d = date?.trim().orEmpty()
        if (d.isBlank()) return null
        val t = time?.trim().orEmpty()
        if (t.isBlank()) return null
        return try {
            val localDate = LocalDate.parse(d)
            val localTime = LocalTime.parse(t)
            val odt = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault()).toOffsetDateTime()
            odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: Exception) { null }
    }

    private fun parseServerToLocal(server: String): Pair<String, String> {
        val odt = OffsetDateTime.parse(server)
        val date = odt.toLocalDate().toString()
        val time = String.format(Locale.US, "%02d:%02d", odt.hour, odt.minute)
        return date to time
    }

    private fun formatDate(millis: Long): String {
        return java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
