package com.example.emotionsai.ui.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.emotionsai.R;
import com.example.emotionsai.databinding.FragmentEmployeeHomeBinding;

public class EmployeeHomeFragment extends Fragment {
    
    private FragmentEmployeeHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.btnTakePhoto.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_employeeHome_to_camera)
        );
        
        binding.btnViewHistory.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_employeeHome_to_stats)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
