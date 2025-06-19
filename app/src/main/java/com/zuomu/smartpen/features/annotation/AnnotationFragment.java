package com.zuomu.smartpen.features.annotation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.zuomu.smartpen.R;

public class AnnotationFragment extends Fragment {

    private AnnotationViewModel viewModel;
    private TabLayout tabTools;
    private ChipGroup chipGroupColors;
    private SeekBar seekBarThickness;
    private TextView textThickness;
    private TextView textEraserSizeTitle;
    private View layoutEraserSize;
    private SeekBar seekBarEraserSize;
    private TextView textEraserSize;
    private View previewArea;
    private MaterialButton btnClear;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_annotation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnnotationViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        tabTools = view.findViewById(R.id.tab_tools);
        chipGroupColors = view.findViewById(R.id.chip_group_colors);
        seekBarThickness = view.findViewById(R.id.seekbar_thickness);
        textThickness = view.findViewById(R.id.text_thickness);
        textEraserSizeTitle = view.findViewById(R.id.text_eraser_size_title);
        layoutEraserSize = view.findViewById(R.id.layout_eraser_size);
        seekBarEraserSize = view.findViewById(R.id.seekbar_eraser_size);
        textEraserSize = view.findViewById(R.id.text_eraser_size);
        previewArea = view.findViewById(R.id.preview_area);
        btnClear = view.findViewById(R.id.btn_clear);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        tabTools.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean isEraser = tab.getPosition() == 1;
                textEraserSizeTitle.setVisibility(isEraser ? View.VISIBLE : View.GONE);
                layoutEraserSize.setVisibility(isEraser ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroupColors.setOnCheckedChangeListener((group, checkedId) -> {
            int color = 0;
            if (checkedId == R.id.chip_red) {
                color = 1;
            } else if (checkedId == R.id.chip_yellow) {
                color = 2;
            } else if (checkedId == R.id.chip_green) {
                color = 3;
            } else if (checkedId == R.id.chip_blue) {
                color = 4;
            }
            viewModel.setAnnotationColor(color);
        });

        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textThickness.setText(String.valueOf(progress));
                viewModel.setLineThickness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarEraserSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textEraserSize.setText(String.valueOf(progress));
                viewModel.setEraserSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnClear.setOnClickListener(v -> {
            // TODO: 清除批注
        });

        btnApply.setOnClickListener(v -> viewModel.saveSettings());
    }

    private void observeViewModel() {
        viewModel.getAnnotationColor().observe(getViewLifecycleOwner(), color -> {
            int checkedId = R.id.chip_black;
            if (color == 1) {
                checkedId = R.id.chip_red;
            } else if (color == 2) {
                checkedId = R.id.chip_yellow;
            } else if (color == 3) {
                checkedId = R.id.chip_green;
            } else if (color == 4) {
                checkedId = R.id.chip_blue;
            }
            chipGroupColors.check(checkedId);
        });

        viewModel.getLineThickness().observe(getViewLifecycleOwner(), thickness -> {
            seekBarThickness.setProgress(thickness);
            textThickness.setText(String.valueOf(thickness));
        });

        viewModel.getEraserSize().observe(getViewLifecycleOwner(), size -> {
            seekBarEraserSize.setProgress(size);
            textEraserSize.setText(String.valueOf(size));
        });
    }
} 