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

    // ✅ чтобы не затирать выбор пользователя повторно
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

    /**
     * ✅ Date + Time pickers
     */
    private fun setupPickers() {
        // ---- DATE pickers ----
        val dpStart = MaterialDatePicker.Builder.datePicker().build()
        vb.inputStart.setOnClickListener {
            dpStart.addOnPositiveButtonClickListener { millis ->
                vb.inputStart.setText(formatDate(millis)) // yyyy-MM-dd
            }
            dpStart.show(parentFragmentManager, "startDate")
        }

        val dpEnd = MaterialDatePicker.Builder.datePicker().build()
        vb.inputEnd.setOnClickListener {
            dpEnd.addOnPositiveButtonClickListener { millis ->
                vb.inputEnd.setText(formatDate(millis)) // yyyy-MM-dd
            }
            dpEnd.show(parentFragmentManager, "endDate")
        }

        // ---- TIME pickers ----
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

            // ✅ собираем ISO с таймзоной устройства
            val startIso = buildIsoDateTimeOrNull(startDate, startTime)
            if (startIso == null) {
                Toast.makeText(requireContext(), "Pick start date & time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val endIso = buildIsoDateTimeOrNull(endDate, endTime) // может быть null

            val parts = adapter.getSelected()

            if (isEditMode) {
                vm.updateEvent(
                    eventId = args.eventId,
                    title = title,
                    startIso = startIso,
                    endIso = endIso,
                    companyId = cid,
                    participantIds = parts
                )
            } else {
                vm.createEvent(
                    title = title,
                    startIso = startIso,
                    endIso = endIso,
                    companyId = cid,
                    participantIds = parts
                )
            }
        }
    }

    private fun setupObservers() {
        vm.employees.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        vm.eventDetails.observe(viewLifecycleOwner) { details ->
            if (details == null) return@observe

            vb.inputTitle.setText(details.title)

            // ✅ распарсим серверный ISO и покажем дату+время
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

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.success.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                vm.successHandled()
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) "Event updated!" else "Event created!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }

        vm.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * ✅ date yyyy-MM-dd + time HH:mm -> ISO_OFFSET_DATE_TIME с TZ устройства: 2026-02-04T14:30:00+05:00
     */
    private fun buildIsoDateTimeOrNull(date: String?, time: String?): String? {
        val d = date?.trim().orEmpty()
        if (d.isBlank()) return null

        // если время пустое — можно считать ошибкой или дефолтить.
        // Ты сказала "00:00 неправильно", поэтому требуем время хотя бы для start.
        val t = time?.trim().orEmpty()
        if (t.isBlank()) return null

        val localDate = LocalDate.parse(d)          // yyyy-MM-dd
        val localTime = LocalTime.parse(t)          // HH:mm
        val zone = ZoneId.systemDefault()           // ✅ TZ устройства

        val odt = ZonedDateTime.of(localDate, localTime, zone).toOffsetDateTime()
        return odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    /**
     * ✅ сервер: 2026-02-14T14:45:15.123+05:00 -> ("2026-02-14", "14:45")
     */
    private fun parseServerToLocal(server: String): Pair<String, String> {
        val odt = OffsetDateTime.parse(server)
        val date = odt.toLocalDate().toString()
        val time = String.format(Locale.US, "%02d:%02d", odt.hour, odt.minute)
        return date to time
    }

    /**
     * millis -> yyyy-MM-dd
     */
    private fun formatDate(millis: Long): String {
        val zone = ZoneId.systemDefault()
        val dt = java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        return dt.toString() // yyyy-MM-dd
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
