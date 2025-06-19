package com.zuomu.smartpen.features.recording;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zuomu.smartpen.R;

public class RecordingFragment extends Fragment {

    private RecordingViewModel viewModel;
    private TextView textSavePath;
    private MaterialButton btnSelectFolder;
    private SwitchMaterial switchMeetingRecording;
    private MaterialButton btnApply;

    private final ActivityResultLauncher<Intent> folderPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    String path = uri.getPath();
                    viewModel.setSavePath(path);
                }
            }
        }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recording, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecordingViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        textSavePath = view.findViewById(R.id.text_save_path);
        btnSelectFolder = view.findViewById(R.id.btn_select_folder);
        switchMeetingRecording = view.findViewById(R.id.switch_meeting_recording);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        btnSelectFolder.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            folderPickerLauncher.launch(intent);
        });

        switchMeetingRecording.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setMeetingRecordingEnabled(isChecked);
        });

        btnApply.setOnClickListener(v -> viewModel.saveSettings(requireContext()));
    }

    private void observeViewModel() {
        viewModel.getSavePath().observe(getViewLifecycleOwner(), path -> {
            textSavePath.setText(path);
        });

        viewModel.getIsMeetingRecordingEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchMeetingRecording.setChecked(enabled);
        });
    }
} 