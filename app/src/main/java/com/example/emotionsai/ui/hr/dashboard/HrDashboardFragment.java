package com.example.emotionsai.ui.hr.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emotionsai.databinding.FragmentHrDashboardBinding;
import com.example.emotionsai.di.ServiceLocator;
import com.google.android.material.chip.Chip;

public class HrDashboardFragment extends Fragment {
    
    private FragmentHrDashboardBinding binding;
    private HrDashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHrDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new HrDashboardViewModel(
            ServiceLocator.INSTANCE.feedbackRepository(requireContext())
        );
        
        setupRecyclerView();
        setupPeriodSelector();
        setupObservers();
        setupRefresh();
    }

    private void setupRecyclerView() {
        binding.rvUserStats.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupPeriodSelector() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            HrDashboardViewModel.Period period;
            int id = checkedIds.get(0);

            if (id == binding.chipWeek.getId()) period = HrDashboardViewModel.Period.WEEK;
            else if (id == binding.chipMonth.getId()) period = HrDashboardViewModel.Period.MONTH;
            else period = HrDashboardViewModel.Period.ALL;

            viewModel.selectPeriod(period);
        });
    }

    private void setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.swipeRefresh.setRefreshing(false);

            if (state instanceof HrDashboardUiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutContent.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.GONE);
            } else if (state instanceof HrDashboardUiState.Success s) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutContent.setVisibility(View.VISIBLE);
                binding.layoutError.setVisibility(View.GONE);

//                displayOverview(s.getOverview());

//                adapter.submitList(s.getByUser().getUsers());

//                HrCharts.INSTANCE.renderEmotionPie(binding.pieChart, s.getTimeline());
//                HrCharts.INSTANCE.renderTimelineStacked(binding.barTimeline, s.getTimeline());

            } else if (state instanceof HrDashboardUiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutContent.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvError.setText(((HrDashboardUiState.Error) state).getMessage());
            }
        });
    }
//
//    private void displayOverview(com.example.emotionsai.data.remote.HrOverviewExResponse overview) {
//        binding.tvTotalCount.setText(String.valueOf(overview.getTotal()));
//        binding.tvAvgConfidence.setText(String.format("%d%%", (int)(overview.getAvg_confidence() * 100)));
//
//        String topEmotion = overview.getTop_emotion();
//        if (topEmotion != null) {
//            binding.tvTopEmotionEmoji.setText(getEmotionEmoji(topEmotion));
//            binding.tvTopEmotionName.setText(topEmotion.toUpperCase());
//        }
//
//        int[] colors = {
//                Color.parseColor("#4CAF50"),
//                Color.parseColor("#2196F3"),
//                Color.parseColor("#FF9800"),
//                Color.parseColor("#F44336"),
//                Color.parseColor("#9C27B0")
//        };
//
//        binding.layoutEmotions.removeAllViews();
//        int index = 0;
//        for (var emotionCount : overview.getEmotions()) {
//            if (index >= 5) break;
//
//            Chip chip = new Chip(requireContext());
//            chip.setText(emotionCount.getEmotion() + " " + (int) emotionCount.getPercent() + "%");
//            chip.setChipBackgroundColorResource(android.R.color.transparent);
//            chip.setChipStrokeWidth(2f);
//            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(colors[index % colors.length]));
//            chip.setTextColor(colors[index % colors.length]);
//            chip.setClickable(false);
//            chip.setCheckable(false);
//
//            binding.layoutEmotions.addView(chip);
//            index++;
//        }
//    }


    private String getEmotionEmoji(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy": return "😊";
            case "sad": return "😢";
            case "angry": return "😠";
            case "surprised": return "😮";
            case "neutral": return "😐";
            case "fear": return "😨";
            case "disgust": return "😖";
            default: return "🙂";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
