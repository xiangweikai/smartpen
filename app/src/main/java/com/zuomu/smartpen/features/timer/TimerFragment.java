package com.zuomu.smartpen.features.timer;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zuomu.smartpen.R;
import java.util.concurrent.TimeUnit;

public class TimerFragment extends Fragment {

    private TimerViewModel viewModel;
    private TextView textCustomDuration;
    private TextView textRemainingTime;
    private SwitchMaterial switchTimer;
    private MaterialButton btnStartTimer;
    private ChipGroup chipGroupDuration;
    private Chip chip30;
    private Chip chip60;
    private Chip chip90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        initViews(view);
        setupListeners();
        observeData();
        viewModel.loadSettings(requireContext());
    }

    private void initViews(View view) {
        textCustomDuration = view.findViewById(R.id.text_custom_duration);
        textRemainingTime = view.findViewById(R.id.text_remaining_time);
        switchTimer = view.findViewById(R.id.switch_timer);
        btnStartTimer = view.findViewById(R.id.btn_start_timer);
        chipGroupDuration = view.findViewById(R.id.chip_group_duration);
        chip30 = view.findViewById(R.id.chip_30);
        chip60 = view.findViewById(R.id.chip_60);
        chip90 = view.findViewById(R.id.chip_90);
    }

    private void setupListeners() {
        chipGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_30) {
                viewModel.setPresetDuration(30);
            } else if (checkedId == R.id.chip_60) {
                viewModel.setPresetDuration(60);
            } else if (checkedId == R.id.chip_90) {
                viewModel.setPresetDuration(90);
            }
        });

        switchTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setEnabled(isChecked);
            viewModel.updateTimerSettings();
        });

        btnStartTimer.setOnClickListener(v -> {
            if (viewModel.getIsRunning().getValue()) {
                viewModel.stopTimer();
                updateStartButtonText(false);
            } else {
                viewModel.startTimer();
                updateStartButtonText(true);
            }
        });
    }

    private void observeData() {
        viewModel.getPresetDuration().observe(getViewLifecycleOwner(), duration -> {
            if (duration != null) {
                textCustomDuration.setText(getString(R.string.timer_custom_duration) + ": " + duration + "分钟");
            }
        });

        viewModel.getRemainingTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null) {
                textRemainingTime.setText(getString(R.string.timer_remaining_time) + ": " + time + "分钟");
            }
        });

        viewModel.getEnabled().observe(getViewLifecycleOwner(), enabled -> {
            if (enabled != null) {
                switchTimer.setChecked(enabled);
            }
        });

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), isRunning -> {
            updateStartButtonText(isRunning);
        });
    }

    private void updateStartButtonText(boolean isRunning) {
        if (btnStartTimer != null) {
            btnStartTimer.setText(isRunning ? R.string.timer_stop : R.string.timer_start);
        }
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.general_error)
            .setMessage(message)
            .setPositiveButton(R.string.general_confirm, null)
            .show();
    }
} 