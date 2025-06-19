package com.zuomu.smartpen.features.voice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.zuomu.smartpen.R;

public class VoiceFragment extends Fragment {

    private VoiceViewModel viewModel;
    private SwitchMaterial switchVoice;
    private RecyclerView recyclerPresetCommands;
    private RecyclerView recyclerCustomCommands;
    private TextInputEditText editCommand;
    private TextInputEditText editTarget;
    private MaterialButton btnSelectTarget;
    private MaterialButton btnAddCommand;
    private SwitchMaterial switchVoiceFeedback;
    private Slider sliderVoiceVolume;
    private Slider sliderMicSensitivity;
    private FloatingActionButton fabVoiceStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VoiceViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
        viewModel.loadSettings(requireContext());
    }

    private void initViews(View view) {
        switchVoice = view.findViewById(R.id.switch_voice);
        recyclerPresetCommands = view.findViewById(R.id.recycler_preset_commands);
        recyclerCustomCommands = view.findViewById(R.id.recycler_custom_commands);
        editCommand = view.findViewById(R.id.edit_command);
        editTarget = view.findViewById(R.id.edit_target);
        btnSelectTarget = view.findViewById(R.id.btn_select_target);
        btnAddCommand = view.findViewById(R.id.btn_add_command);
        switchVoiceFeedback = view.findViewById(R.id.switch_voice_feedback);
        sliderVoiceVolume = view.findViewById(R.id.slider_voice_volume);
        sliderMicSensitivity = view.findViewById(R.id.slider_mic_sensitivity);
        fabVoiceStatus = view.findViewById(R.id.fab_voice_status);
    }

    private void setupListeners() {
        switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setEnabled(isChecked);
        });

        switchVoiceFeedback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setVoiceFeedback(isChecked);
        });

        sliderVoiceVolume.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setVolume((int) (value * 100));
            }
        });

        sliderMicSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setMicSensitivity((int) (value * 100));
            }
        });

        btnAddCommand.setOnClickListener(v -> {
            String command = editCommand.getText().toString();
            String target = editTarget.getText().toString();
            if (!command.isEmpty() && !target.isEmpty()) {
                viewModel.addCustomCommand(command, target);
                editCommand.setText("");
                editTarget.setText("");
            }
        });

        btnSelectTarget.setOnClickListener(v -> {
            // TODO: 实现目标选择逻辑
        });

        fabVoiceStatus.setOnClickListener(v -> {
            // TODO: 实现语音识别状态切换逻辑
        });
    }

    private void observeViewModel() {
        viewModel.getIsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchVoice.setChecked(enabled);
        });

        viewModel.getVolume().observe(getViewLifecycleOwner(), volume -> {
            sliderVoiceVolume.setValue(volume / 100f);
        });

        viewModel.getMicSensitivity().observe(getViewLifecycleOwner(), sensitivity -> {
            sliderMicSensitivity.setValue(sensitivity / 100f);
        });
    }
} 