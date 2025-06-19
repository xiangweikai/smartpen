package com.zuomu.smartpen.features.magnifier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zuomu.smartpen.R;

public class MagnifierFragment extends Fragment {

    private MagnifierViewModel viewModel;
    private SwitchMaterial switchMagnifier;
    private SeekBar seekBarMagnifierSize;
    private TextView textMagnifierSize;
    private ChipGroup chipGroupZoom;
    private RadioGroup radioGroupShape;
    private View magnifierPreview;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_magnifier, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MagnifierViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        switchMagnifier = view.findViewById(R.id.switch_magnifier);
        seekBarMagnifierSize = view.findViewById(R.id.seekbar_magnifier_size);
        textMagnifierSize = view.findViewById(R.id.text_magnifier_size);
        chipGroupZoom = view.findViewById(R.id.chip_group_zoom);
        radioGroupShape = view.findViewById(R.id.radio_group_shape);
        magnifierPreview = view.findViewById(R.id.magnifier_preview);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        switchMagnifier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setEnabled(isChecked);
        });

        seekBarMagnifierSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMagnifierSize.setText(String.valueOf(progress));
                viewModel.setMagnifierSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chipGroupZoom.setOnCheckedChangeListener((group, checkedId) -> {
            int zoomLevel = 2;
            if (checkedId == R.id.chip_3x) {
                zoomLevel = 3;
            } else if (checkedId == R.id.chip_4x) {
                zoomLevel = 4;
            } else if (checkedId == R.id.chip_5x) {
                zoomLevel = 5;
            }
            viewModel.setZoomLevel(zoomLevel);
        });

        btnApply.setOnClickListener(v -> viewModel.saveSettings());
    }

    private void observeViewModel() {
        viewModel.getMagnifierSize().observe(getViewLifecycleOwner(), size -> {
            seekBarMagnifierSize.setProgress(size);
            textMagnifierSize.setText(String.valueOf(size));
            updateMagnifierPreview();
        });

        viewModel.getZoomLevel().observe(getViewLifecycleOwner(), zoom -> {
            int checkedId = R.id.chip_2x;
            if (zoom == 3) {
                checkedId = R.id.chip_3x;
            } else if (zoom == 4) {
                checkedId = R.id.chip_4x;
            } else if (zoom == 5) {
                checkedId = R.id.chip_5x;
            }
            chipGroupZoom.check(checkedId);
            updateMagnifierPreview();
        });

        viewModel.getIsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchMagnifier.setChecked(enabled);
            updateMagnifierPreview();
        });
    }

    private void updateMagnifierPreview() {
        Integer size = viewModel.getMagnifierSize().getValue();
        Integer zoom = viewModel.getZoomLevel().getValue();
        Boolean enabled = viewModel.getIsEnabled().getValue();

        if (size != null && zoom != null && enabled != null) {
            // TODO: 实现放大镜效果预览
        }
    }
} 