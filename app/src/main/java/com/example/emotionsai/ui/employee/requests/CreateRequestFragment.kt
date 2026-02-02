package com.example.emotionsai.ui.employee.requests

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentCreateRequestBinding
import com.example.emotionsai.di.ServiceLocator

class CreateRequestFragment : Fragment(R.layout.fragment_create_request) {

    private var _vb: FragmentCreateRequestBinding? = null
    private val vb get() = _vb!!

    private val vm: CreateRequestViewModel by viewModels {
        ServiceLocator.createRequestVMFactory(requireContext())
    }

    private var selectedTypeId: Int? = null
    private var selectedHrId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentCreateRequestBinding.bind(view)

        // types
        vm.types.observe(viewLifecycleOwner) { list ->
            vb.dropType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list.map { it.name }))
            vb.dropType.setOnItemClickListener { _, _, pos, _ -> selectedTypeId = list[pos].id }
        }

        // hr list
        vm.hrs.observe(viewLifecycleOwner) { list ->
            vb.dropHr.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list.map { it.name }))
            vb.dropHr.setOnItemClickListener { _, _, pos, _ -> selectedHrId = list[pos].id }
        }

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        vm.created.observe(viewLifecycleOwner) { created ->
            if (created != null) {
                vm.createdHandled()
                val action = CreateRequestFragmentDirections
                    .actionCreateRequestFragmentToEmployeeRequestDetailsFragment(created.id)
                findNavController().navigate(action)
            }
        }

        vb.btnCreate.setOnClickListener {
            val typeId = selectedTypeId
            val hrId = selectedHrId
            val comment = vb.inputComment.text?.toString()?.trim().orEmpty()

            if (typeId == null) return@setOnClickListener toast("Выберите тип")
            if (hrId == null) return@setOnClickListener toast("Выберите HR")
            if (comment.isBlank()) return@setOnClickListener toast("Введите комментарий")

            vm.create(typeId, hrId, comment)
        }

        vm.loadRefs() // types + hr-list
    }

    private fun toast(s: String) =
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
